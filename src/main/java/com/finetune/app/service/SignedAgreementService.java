package com.finetune.app.service;

import com.finetune.app.model.Shop;
import com.finetune.app.model.AgreementTemplate;
import com.finetune.app.model.Customer;
import com.finetune.app.model.SignedAgreement;
import com.finetune.app.model.WorkOrder;
import com.finetune.app.repository.sql.SignedAgreementSqlRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing signed agreements.
 * Signed agreements are immutable records - once created, they cannot be modified.
 */
@Service
public class SignedAgreementService {

    private final SignedAgreementSqlRepository signedAgreementRepository;
    private final AgreementTemplateService agreementTemplateService;
    private final AgreementPdfService agreementPdfService;

    public SignedAgreementService(
            SignedAgreementSqlRepository signedAgreementRepository,
            AgreementTemplateService agreementTemplateService,
            AgreementPdfService agreementPdfService) {
        this.signedAgreementRepository = signedAgreementRepository;
        this.agreementTemplateService = agreementTemplateService;
        this.agreementPdfService = agreementPdfService;
    }

    /**
     * Complete workflow to create a signed agreement.
     * 
     * Process:
     * 1. Load active AgreementTemplate for shop
     * 2. Generate PDF via AgreementPdfService
     * 3. Upload PDF to Cloudflare R2
     * 4. Compute SHA-256 hash of PDF bytes
     * 5. Create SignedAgreement entity
     * 6. Persist and associate with WorkOrder
     * 
     * @param workOrder The work order to associate with the agreement
     * @param shop The shop to load the agreement template for
     * @param signatureName The name used for the signature
     * @param signatureIp The IP address of the person signing
     * @param signatureUserAgent The user agent string of the browser
     * @return The created and persisted SignedAgreement with storage key and document hash
     * @throws IllegalArgumentException if no active template found or PDF generation fails
     */
    @Transactional
        public SignedAgreement createSignedAgreementWorkflow(
            WorkOrder workOrder,
            Shop shop,
            String signatureName,
            String signatureIp,
            String signatureUserAgent,
            String signatureImageBase64) {
        
        try {
            System.out.println("[SignedAgreementService] createSignedAgreementWorkflow called for workOrderId=" + workOrder.getId());
            // 1. Load active AgreementTemplate for shop
                AgreementTemplate agreementTemplate = agreementTemplateService.getActiveTemplate(shop)
                    .orElseThrow(() -> new IllegalArgumentException(
                        "No active agreement template found for shop: " + shop.getName()));
                System.out.println("[SignedAgreementService] Loaded agreement template: " + agreementTemplate.getId());
            
            Customer customer = workOrder.getCustomer();
            
            // 2 & 3. Generate PDF (in memory) and upload to Cloudflare R2
            // This generates the PDF once and returns both bytes and storage key
                    String pdfStorageKey = agreementPdfService.generateAndUploadAgreementPdf(
                        agreementTemplate,
                        workOrder,
                        customer,
                        signatureName,
                        signatureIp,
                        signatureUserAgent,
                        signatureImageBase64
                    );
                    System.out.println("[SignedAgreementService] PDF generated and uploaded. Storage key: " + pdfStorageKey);
            
            // Generate PDF bytes again for hash computation
            // Note: We need the bytes for hashing, so we generate twice
            // This is acceptable as PDF generation is deterministic
                    byte[] pdfBytes = agreementPdfService.generateAgreementPdfBytes(
                        agreementTemplate,
                        workOrder,
                        customer,
                        signatureName,
                        signatureIp,
                        signatureUserAgent,
                        signatureImageBase64
                    );
                    System.out.println("[SignedAgreementService] PDF bytes generated for hash.");
            
            // 4. Compute SHA-256 hash of PDF bytes
            String documentHash = computeDocumentHash(pdfBytes);
            System.out.println("[SignedAgreementService] Document hash computed: " + documentHash);
            
            // 5. Create SignedAgreement entity
            LocalDateTime signedAt = LocalDateTime.now();
                SignedAgreement signedAgreement = new SignedAgreement(
                    workOrder,
                    customer,
                    agreementTemplate,
                    pdfStorageKey,
                    signatureName,
                    signedAt,
                    documentHash
                );
                System.out.println("[SignedAgreementService] SignedAgreement entity created.");
            
            // Populate optional fields
            signedAgreement.setSignatureIp(signatureIp);
            signedAgreement.setSignatureUserAgent(signatureUserAgent);
            
            // 6. Persist SignedAgreement and associate with WorkOrder
            SignedAgreement saved = signedAgreementRepository.save(signedAgreement);
            System.out.println("[SignedAgreementService] SignedAgreement saved to DB. ID: " + saved.getId());
            return saved;
            
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Failed to create signed agreement: " + e.getMessage(), e);
        }
    }

    /**
     * Compute SHA-256 hash of document bytes for integrity verification.
     * 
     * @param documentBytes The document bytes to hash
     * @return Hex-encoded SHA-256 hash
     * @throws RuntimeException if hash computation fails
     */
    private String computeDocumentHash(byte[] documentBytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(documentBytes);
            
            // Convert to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute document hash", e);
        }
    }

    /**
     * Create a new signed agreement record.
     * This record is immutable and cannot be updated after creation.
     * 
     * @param signedAgreement The signed agreement to create
     * @return The created signed agreement
     */
    @Transactional
    public SignedAgreement createSignedAgreement(SignedAgreement signedAgreement) {
        return signedAgreementRepository.save(signedAgreement);
    }

    /**
     * Get a signed agreement by its ID.
     * 
     * @param id The signed agreement ID
     * @return Optional containing the signed agreement if found
     */
    public Optional<SignedAgreement> getSignedAgreementById(String id) {
        SignedAgreement agreement = signedAgreementRepository.findById(id);
        return agreement == null ? Optional.empty() : Optional.of(agreement);
    }

    /**
     * Get all signed agreements for a specific work order.
     * 
     * @param workOrder The work order to find agreements for
     * @return List of signed agreements for the work order
     */
    public List<SignedAgreement> getSignedAgreementsByWorkOrder(WorkOrder workOrder) {
        return signedAgreementRepository.findByWorkOrder(workOrder.getId());
    }

    /**
     * Get all signed agreements for a specific customer.
     * 
     * @param customer The customer to find agreements for
     * @return List of signed agreements for the customer
     */
    public List<SignedAgreement> getSignedAgreementsByCustomer(Customer customer) {
        return signedAgreementRepository.findByCustomer(customer.getId());
    }

    /**
     * Get all signed agreements using a specific template.
     * 
     * @param agreementTemplate The template to find agreements for
     * @return List of signed agreements using the template
     */
    public List<SignedAgreement> getSignedAgreementsByTemplate(AgreementTemplate agreementTemplate) {
        return signedAgreementRepository.findByAgreementTemplate(agreementTemplate.getId());
    }

    /**
     * Get a signed agreement by its document hash.
     * Useful for verifying document integrity and preventing duplicates.
     * 
     * @param documentHash The hash of the document
     * @return Optional containing the signed agreement if found
     */
    public Optional<SignedAgreement> getSignedAgreementByDocumentHash(String documentHash) {
        List<SignedAgreement> agreements = signedAgreementRepository.findByDocumentHash(documentHash);
        return agreements.isEmpty() ? Optional.empty() : Optional.of(agreements.get(0));
    }

    /**
     * Get all signed agreements for a customer within a date range.
     * 
     * @param customer The customer to find agreements for
     * @param startDate The start date
     * @param endDate The end date
     * @return List of signed agreements within the date range
     */
    public List<SignedAgreement> getSignedAgreementsByCustomerAndDateRange(
            Customer customer, LocalDateTime startDate, LocalDateTime endDate) {
        return signedAgreementRepository.findByCustomerAndSignedAtBetween(customer.getId(), startDate, endDate);
    }

    /**
     * Get all signed agreements within a date range.
     * 
     * @param startDate The start date
     * @param endDate The end date
     * @return List of signed agreements within the date range
     */
    public List<SignedAgreement> getSignedAgreementsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return signedAgreementRepository.findBySignedAtBetween(startDate, endDate);
    }

    /**
     * Get the most recent signed agreement for a customer.
     * 
     * @param customer The customer to find the agreement for
     * @return Optional containing the most recent signed agreement if found
     */
    public Optional<SignedAgreement> getMostRecentSignedAgreement(Customer customer) {
        return signedAgreementRepository.findFirstByCustomerOrderBySignedAtDesc(customer.getId());
    }

    /**
     * Check if a work order has a signed agreement.
     * 
     * @param workOrder The work order to check
     * @return true if the work order has a signed agreement
     */
    public boolean workOrderHasSignedAgreement(WorkOrder workOrder) {
        return signedAgreementRepository.existsByWorkOrder(workOrder.getId());
    }

    /**
     * Find signed agreement by work order ID.
     * Returns the first (and typically only) signed agreement for the work order.
     * 
     * @param workOrderId The work order ID
     * @return SignedAgreement if found, null otherwise
     */
    public SignedAgreement findByWorkOrderId(Long workOrderId) {
        return signedAgreementRepository.findFirstByWorkOrderId(workOrderId)
                .orElse(null);
    }

    /**
     * Get all signed agreements.
     * Use with caution - may return a large dataset.
     * 
     * @return List of all signed agreements
     */
    public List<SignedAgreement> getAllSignedAgreements() {
        return signedAgreementRepository.findAll();
    }

    /**
     * Verify the integrity of a signed agreement by comparing its hash.
     * 
     * @param id The signed agreement ID
     * @param expectedHash The expected document hash
     * @return true if the hashes match
     */
    public boolean verifyDocumentIntegrity(String id, String expectedHash) {
        SignedAgreement agreement = signedAgreementRepository.findById(id);
        return agreement != null && agreement.getDocumentHash().equals(expectedHash);
    }
}

