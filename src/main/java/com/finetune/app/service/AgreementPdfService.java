package com.finetune.app.service;

import com.finetune.app.model.Shop;
import com.finetune.app.model.AgreementTemplate;
import com.finetune.app.model.Customer;
import com.finetune.app.model.WorkOrder;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;


import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for generating agreement PDF documents and uploading to R2 storage.
 * Uses Thymeleaf for HTML templating and Flying Saucer for PDF generation.
 * PDFs are generated in memory and uploaded directly to Cloudflare R2.
 */
@Service
public class AgreementPdfService {

    private final TemplateEngine templateEngine;
    private final ObjectStorageService objectStorageService;

    public AgreementPdfService(TemplateEngine templateEngine, ObjectStorageService objectStorageService) {
        this.templateEngine = templateEngine;
        this.objectStorageService = objectStorageService;
    }

    /**
     * Generate a PDF agreement document, upload to R2, and return storage key.
     * PDF is generated in memory and uploaded directly without writing to disk.
     *
     * @param agreementTemplate The agreement template to use
     * @param workOrder The work order associated with this agreement
     * @param customer The customer signing the agreement
     * @param signatureName The name used for the signature
     * @param ipAddress The IP address of the person signing
     * @param userAgent The user agent string of the browser
     * @return Storage key for the uploaded PDF (e.g., "agreements/{shopId}/{workOrderId}/{uuid}.pdf")
     * @throws Exception if PDF generation or upload fails
     */
        public String generateAndUploadAgreementPdf(
            AgreementTemplate agreementTemplate,
            WorkOrder workOrder,
            Customer customer,
            String signatureName,
            String ipAddress,
            String userAgent,
            String signatureImageBase64) throws Exception {

        // Generate PDF in memory
        ByteArrayOutputStream pdfStream = generatePdfStream(
            agreementTemplate, workOrder, customer, signatureName, ipAddress, userAgent, signatureImageBase64);
        
        byte[] pdfBytes = pdfStream.toByteArray();
        
        // Generate storage key: agreements/{shopId}/{workOrderId}/{uuid}.pdf
        Shop shop = agreementTemplate.getShop();
        String storageKey = String.format("agreements/%s/%s/%s.pdf", 
                shop != null ? shop.getId() : "unknown",
                workOrder.getId(),
                UUID.randomUUID().toString());
        
        // Upload to R2 (directly from memory, no disk writes)
        objectStorageService.uploadPdf(pdfBytes, storageKey);

        // Also save to user's Desktop (Windows only, safe for your dev environment)
        try {
            String userHome = System.getProperty("user.home");
            System.out.println("[AgreementPdfService] user.home=" + userHome);
            String desktopPath = userHome + java.io.File.separator + "Desktop";
            System.out.println("[AgreementPdfService] desktopPath=" + desktopPath);
            String fileName = String.format("Agreement_%s_%s.pdf",
                    shop != null ? shop.getId() : "unknown",
                    workOrder.getId());
            System.out.println("[AgreementPdfService] fileName=" + fileName);
            java.nio.file.Path outputPath = java.nio.file.Paths.get(desktopPath, fileName);
            System.out.println("[AgreementPdfService] outputPath=" + outputPath.toString());
            java.nio.file.Files.write(outputPath, pdfBytes);
            System.out.println("[AgreementPdfService] PDF successfully written to Desktop: " + outputPath);
        } catch (Exception e) {
            // Log but do not fail the upload if desktop write fails
            System.err.println("[AgreementPdfService] Failed to write PDF to Desktop: " + e.getMessage());
            e.printStackTrace();
        }

        return storageKey;
    }

    /**
     * Generate a PDF agreement document and return as byte array.
     * This method is used when you need the PDF bytes (e.g., for hash generation).
     * Does NOT upload to storage.
     *
     * @param agreementTemplate The agreement template to use
     * @param workOrder The work order associated with this agreement
     * @param customer The customer signing the agreement
     * @param signatureName The name used for the signature
     * @param ipAddress The IP address of the person signing
     * @param userAgent The user agent string of the browser
     * @return PDF bytes
     * @throws Exception if PDF generation fails
     */
        public byte[] generateAgreementPdfBytes(
            AgreementTemplate agreementTemplate,
            WorkOrder workOrder,
            Customer customer,
            String signatureName,
            String ipAddress,
            String userAgent,
            String signatureImageBase64) throws Exception {

        ByteArrayOutputStream pdfStream = generatePdfStream(
            agreementTemplate, workOrder, customer, signatureName, ipAddress, userAgent, signatureImageBase64);
        return pdfStream.toByteArray();
        }

    /**
     * Internal method to generate PDF stream.
     *
     * @param agreementTemplate The agreement template to use
     * @param workOrder The work order associated with this agreement
     * @param customer The customer signing the agreement
     * @param signatureName The name used for the signature
     * @param ipAddress The IP address of the person signing
     * @param userAgent The user agent string of the browser
     * @return ByteArrayOutputStream containing the PDF document
     * @throws Exception if PDF generation fails
     */
        private ByteArrayOutputStream generatePdfStream(
            AgreementTemplate agreementTemplate,
            WorkOrder workOrder,
            Customer customer,
            String signatureName,
            String ipAddress,
            String userAgent,
            String signatureImageBase64) throws Exception {

        // Create Thymeleaf context with all required variables
        Context context = new Context();
        // Signature image (base64 PNG)
        context.setVariable("signatureImageBase64", signatureImageBase64);
        // Template information
        context.setVariable("title", agreementTemplate.getTitle());
        context.setVariable("agreementText", agreementTemplate.getAgreementText());
        context.setVariable("jurisdictionState", agreementTemplate.getJurisdictionState());
        
        // Shop information
        Shop shop = agreementTemplate.getShop();
        context.setVariable("shopName", shop != null ? shop.getName() : "");
        
        // Logo URL: Use template's logoUrl if set, otherwise fall back to shop's logoUrl
        String logoUrl = agreementTemplate.getLogoUrl();
        if ((logoUrl == null || logoUrl.trim().isEmpty()) && shop != null) {
            logoUrl = shop.getLogoUrl();
        }
        context.setVariable("logoUrl", logoUrl);
        
        // Shop address from Location
        if (shop != null && shop.getLocation() != null) {
            context.setVariable("shopAddress", shop.getLocation().getAddress());
            context.setVariable("shopCity", shop.getLocation().getCity());
            context.setVariable("shopState", shop.getLocation().getState());
            context.setVariable("shopZipCode", shop.getLocation().getZipCode());
        } else {
            context.setVariable("shopAddress", "");
            context.setVariable("shopCity", "");
            context.setVariable("shopState", "");
            context.setVariable("shopZipCode", "");
        }
        
        // Customer information
        context.setVariable("customerName", customer.getFirstName() + " " + customer.getLastName());
        context.setVariable("customerEmail", customer.getEmail());
        context.setVariable("customerPhone", customer.getPhone());
        
        // Work order information
        context.setVariable("workOrderId", workOrder.getId());
        
        // Signature information
        context.setVariable("signatureName", signatureName);
        context.setVariable("signedAt", LocalDateTime.now());
        context.setVariable("ipAddress", ipAddress != null ? ipAddress : "Unknown");
        context.setVariable("userAgent", userAgent);
        
        // Document metadata
        context.setVariable("documentId", UUID.randomUUID().toString());
        context.setVariable("generatedAt", LocalDateTime.now());

        // Render HTML from Thymeleaf template
        String htmlContent = templateEngine.process("agreement-pdf", context);

        // Convert HTML to PDF
        return convertHtmlToPdf(htmlContent);
    }

    /**
     * Convert HTML content to PDF using Flying Saucer.
     *
     * @param htmlContent The HTML content to convert
     * @return ByteArrayOutputStream containing the PDF document
     * @throws Exception if conversion fails
     */
    private ByteArrayOutputStream convertHtmlToPdf(String htmlContent) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        try {
            // Create ITextRenderer instance
            ITextRenderer renderer = new ITextRenderer();
            
            // Set the HTML content
            renderer.setDocumentFromString(htmlContent);
            
            // Layout the document
            renderer.layout();
            
            // Create PDF
            renderer.createPDF(outputStream);
            
            return outputStream;
        } catch (Exception e) {
            throw new Exception("Failed to generate PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Generate a preview of the agreement HTML without converting to PDF.
     * Useful for testing and debugging templates.
     *
     * @param agreementTemplate The agreement template to use
     * @param workOrder The work order associated with this agreement
     * @param customer The customer signing the agreement
     * @param signatureName The name used for the signature
     * @param ipAddress The IP address of the person signing
     * @return HTML string
     */
    public String generateAgreementHtmlPreview(
            AgreementTemplate agreementTemplate,
            WorkOrder workOrder,
            Customer customer,
            String signatureName,
            String ipAddress) {

        Context context = new Context();
        
        // Template information
        context.setVariable("title", agreementTemplate.getTitle());
        context.setVariable("agreementText", agreementTemplate.getAgreementText());
        context.setVariable("jurisdictionState", agreementTemplate.getJurisdictionState());
        
        // Shop information
        Shop shop = agreementTemplate.getShop();
        context.setVariable("shopName", shop != null ? shop.getName() : "");
        
        // Logo URL: Use template's logoUrl if set, otherwise fall back to shop's logoUrl
        String logoUrl = agreementTemplate.getLogoUrl();
        if ((logoUrl == null || logoUrl.trim().isEmpty()) && shop != null) {
            logoUrl = shop.getLogoUrl();
        }
        context.setVariable("logoUrl", logoUrl);
        
        // Shop address from Location
        if (shop != null && shop.getLocation() != null) {
            context.setVariable("shopAddress", shop.getLocation().getAddress());
            context.setVariable("shopCity", shop.getLocation().getCity());
            context.setVariable("shopState", shop.getLocation().getState());
            context.setVariable("shopZipCode", shop.getLocation().getZipCode());
        } else {
            context.setVariable("shopAddress", "");
            context.setVariable("shopCity", "");
            context.setVariable("shopState", "");
            context.setVariable("shopZipCode", "");
        }
        
        // Customer information
        context.setVariable("customerName", customer.getFirstName() + " " + customer.getLastName());
        context.setVariable("customerEmail", customer.getEmail());
        context.setVariable("customerPhone", customer.getPhone());
        
        // Work order information
        context.setVariable("workOrderId", workOrder.getId());
        
        // Signature information
        context.setVariable("signatureName", signatureName);
        context.setVariable("signedAt", LocalDateTime.now());
        context.setVariable("ipAddress", ipAddress != null ? ipAddress : "Unknown");
        context.setVariable("userAgent", "Preview Mode");
        
        // Document metadata
        context.setVariable("documentId", "PREVIEW-" + UUID.randomUUID().toString());
        context.setVariable("generatedAt", LocalDateTime.now());

        return templateEngine.process("agreement-pdf", context);
    }
}
