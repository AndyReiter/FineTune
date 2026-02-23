// Customer Work Order Creation - Main JavaScript
// API Base URL - adjust if needed
const API_BASE = window.location.origin;

// ==================== State Management ====================
const state = {
  currentStep: 1,
  customer: {
    name: '',
    email: '',
    phone: '',
    customerId: null
  },
  availableEquipment: {
    existingSkis: [],
    existingBoots: []
  },
  equipmentItems: [], // Array of {ski, serviceType, boot, binding} objects
  currentItem: {
    selectedSki: null,
    selectedBoot: null,
    newSki: null,
    newBoot: null,
    serviceType: '',
    binding: {
      brand: '',
      model: '',
      heightInches: null,
      weight: null,
      age: null,
      abilityLevel: null
    }
  },
  agreement: {
    accepted: false,
    signedName: '',
    signatureImageBase64: '',
    acceptedAt: null,
    scrolledToBottom: false
  }
};

// ==================== Initialization ====================
document.addEventListener('DOMContentLoaded', () => {
  initializeEventListeners();
});

function initializeEventListeners() {
  // Step 1: Customer Form
  document.getElementById('customerForm').addEventListener('submit', handleCustomerFormSubmit);

  // Step 2: Equipment Selection
  document.getElementById('addNewSkiBtn').addEventListener('click', showNewSkiForm);
  document.getElementById('cancelNewSkiBtn').addEventListener('click', hideNewSkiForm);
  document.getElementById('saveNewSkiBtn').addEventListener('click', handleSaveNewSki);
  document.getElementById('backToStep1').addEventListener('click', () => goToStep(1));
  document.getElementById('continueToStep3').addEventListener('click', handleContinueToService);

  // Step 3: Service Form
  document.getElementById('serviceForm').addEventListener('submit', handleServiceFormSubmit);
  document.getElementById('backToStep2').addEventListener('click', () => goToStep(2));
  
  // Step 3: Mount Boot Selection (conditional)
  document.getElementById('addNewMountBootBtn').addEventListener('click', showNewMountBootForm);
  document.getElementById('cancelNewMountBootBtn').addEventListener('click', hideNewMountBootForm);
  document.getElementById('saveNewMountBootBtn').addEventListener('click', handleSaveNewMountBoot);
  document.getElementById('backFromMountBoot').addEventListener('click', () => {
    // Go back to service form
    document.getElementById('bootSelectionForMount').style.display = 'none';
    document.getElementById('bindingInfoSection').style.display = 'none';
    document.getElementById('newBootProfileFields').style.display = 'none';
    document.getElementById('serviceFormButtons').style.display = 'flex';
    // Clear binding form
    clearBindingForm();
  });
  document.getElementById('continueFromMountBoot').addEventListener('click', handleContinueFromMountBoot);

  // Equipment Choice Dialog
  document.getElementById('addAnotherEquipmentBtn').addEventListener('click', handleAddAnotherEquipment);
  document.getElementById('proceedToReviewBtn').addEventListener('click', handleProceedToReview);

  // Step 5: Agreement (conditional - MOUNT services only)
  const agreementScrollBox = document.getElementById('agreementScrollBox');
  if (agreementScrollBox) {
    agreementScrollBox.addEventListener('scroll', handleAgreementScroll);
  }
  const signatureName = document.getElementById('signatureName');
  if (signatureName) {
    signatureName.addEventListener('input', handleSignatureNameInput);
  }
  const agreementCheckbox = document.getElementById('agreementCheckbox');
  if (agreementCheckbox) {
    agreementCheckbox.addEventListener('change', function() {
      // Toggle checked class on label
      const checkboxLabel = document.getElementById('agreementCheckboxLabel');
      if (checkboxLabel) {
        if (this.checked) {
          checkboxLabel.classList.add('checked');
        } else {
          checkboxLabel.classList.remove('checked');
        }
      }
      
      validateAgreementForm();
      saveAgreementToLocalStorage();
    });
  }
  const clearSignatureBtn = document.getElementById('clearSignatureBtn');
  if (clearSignatureBtn) {
    clearSignatureBtn.addEventListener('click', clearSignature);
  }
  const backFromAgreement = document.getElementById('backFromAgreement');
  if (backFromAgreement) {
    backFromAgreement.addEventListener('click', () => goToStep(3));
  }
  const continueFromAgreement = document.getElementById('continueFromAgreement');
  if (continueFromAgreement) {
    continueFromAgreement.addEventListener('click', handleContinueFromAgreement);
  }

  // Step 4: Confirmation
  document.getElementById('backToStep3').addEventListener('click', () => goToStep(3));
  document.getElementById('submitWorkOrder').addEventListener('click', handleSubmitWorkOrder);
}

// ==================== Navigation ====================
function goToStep(stepNumber) {
  // Hide all steps
  document.querySelectorAll('.step-content').forEach(step => {
    step.style.display = 'none';
  });

  // Show target step
  document.getElementById(`step${stepNumber}`).style.display = 'block';

  // Reset step 3 boot selection visibility
  if (stepNumber === 3) {
    document.getElementById('bootSelectionForMount').style.display = 'none';
    document.getElementById('bindingInfoSection').style.display = 'none';
    document.getElementById('serviceFormButtons').style.display = 'flex';
  }

  // Update progress bar
  document.querySelectorAll('.progress-step').forEach((step, index) => {
    const stepNum = index + 1;
    step.classList.remove('active', 'completed');
    
    if (stepNum === stepNumber) {
      step.classList.add('active');
    } else if (stepNum < stepNumber) {
      step.classList.add('completed');
    }
  });

  // Update state
  state.currentStep = stepNumber;

  // Scroll to top
  window.scrollTo({ top: 0, behavior: 'smooth' });
}

function showError(elementId, message) {
  const errorElement = document.getElementById(elementId);
  errorElement.textContent = message;
  errorElement.classList.add('show');
}

function hideError(elementId) {
  const errorElement = document.getElementById(elementId);
  errorElement.textContent = '';
  errorElement.classList.remove('show');
}

// ==================== Step 1: Customer Information ====================
async function handleCustomerFormSubmit(e) {
  e.preventDefault();
  hideError('customerError');

  const name = document.getElementById('customerName').value.trim();
  const email = document.getElementById('customerEmail').value.trim();
  const phone = document.getElementById('customerPhone').value.trim();

  // Validate inputs
  if (!name || !email || !phone) {
    showError('customerError', 'Please fill in all required fields.');
    return;
  }

  // Store customer info
  state.customer.name = name;
  state.customer.email = email;
  state.customer.phone = phone;

  // Show loading and move to step 2
  goToStep(2);
  showEquipmentLoading();

  try {
    // Call lookup endpoint
    const response = await fetch(
      `${API_BASE}/api/public/workorders/lookup-equipment?` +
      `name=${encodeURIComponent(name)}&` +
      `email=${encodeURIComponent(email)}&` +
      `phone=${encodeURIComponent(phone)}`
    );

    if (!response.ok) {
      throw new Error('Failed to lookup customer equipment');
    }

    const data = await response.json();
    
    // Store customer ID and available equipment
    state.customer.customerId = data.customerId;
    state.availableEquipment.existingSkis = data.equipment || [];
    state.availableEquipment.existingBoots = data.boots || [];

    // Display equipment options
    displayEquipmentOptions();

  } catch (error) {
    console.error('Error looking up equipment:', error);
    showError('equipmentError', 'Failed to load your equipment. Please try again.');
    hideEquipmentLoading();
  }
}

function showEquipmentLoading() {
  document.getElementById('equipmentLoading').style.display = 'block';
  document.getElementById('existingSkisSection').style.display = 'none';
  document.getElementById('addNewSkiButtonContainer').style.display = 'none';
}

function hideEquipmentLoading() {
  document.getElementById('equipmentLoading').style.display = 'none';
}

function displayEquipmentOptions() {
  hideEquipmentLoading();

  const existingSkisSection = document.getElementById('existingSkisSection');
  const existingSkisList = document.getElementById('existingSkisList');
  const addNewSkiButtonContainer = document.getElementById('addNewSkiButtonContainer');

  if (state.availableEquipment.existingSkis.length === 0) {
    // No existing equipment - show new ski form directly
    document.getElementById('equipmentSubtitle').textContent = 'Add your ski information';
    showNewSkiForm();
  } else {
    // Show existing skis
    existingSkisSection.style.display = 'block';
    addNewSkiButtonContainer.style.display = 'block';

    existingSkisList.innerHTML = '';
    
    state.availableEquipment.existingSkis.forEach(ski => {
      const card = createEquipmentCard(ski);
      existingSkisList.appendChild(card);
    });
  }
}

function createEquipmentCard(ski) {
  const card = document.createElement('div');
  card.className = 'equipment-card';
  card.dataset.equipmentId = ski.id;

  // Format last service info if available
  let serviceInfo = '';
  if (ski.lastServiceType) {
    serviceInfo = `Last Service: ${ski.lastServiceType}`;
    if (ski.lastServicedDate) {
      const serviceDate = new Date(ski.lastServicedDate);
      const formattedDate = serviceDate.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
      serviceInfo += ` (${formattedDate})`;
    }
  } else {
    serviceInfo = 'No service history';
  }

  card.innerHTML = `
    <div class="equipment-card-header">
      <div class="equipment-icon">⛷️</div>
      <div>
        <div class="equipment-card-title">${ski.brand} ${ski.model}</div>
        <div class="equipment-card-details">${serviceInfo}</div>
      </div>
    </div>
  `;

  card.addEventListener('click', () => handleSkiSelection(ski, card));

  return card;
}

function handleSkiSelection(ski, cardElement) {
  // Clear previous selection
  document.querySelectorAll('.equipment-card').forEach(c => c.classList.remove('selected'));
  
  // Mark as selected
  cardElement.classList.add('selected');
  
  // Store selection in current item
  state.currentItem.selectedSki = ski;
  state.currentItem.newSki = null;

  // Hide new ski form if open
  hideNewSkiForm();

  // Hide boot selection (only shown if service type is MOUNT)
  document.getElementById('bootSelectionSection').style.display = 'none';

  // Enable continue button
  updateContinueButton();
}

// ==================== New Ski Form ====================
function showNewSkiForm() {
  document.getElementById('newSkiForm').style.display = 'block';
  document.getElementById('addNewSkiButtonContainer').style.display = 'none';
  
  // Clear any selected existing ski
  document.querySelectorAll('#existingSkisList .equipment-card').forEach(c => c.classList.remove('selected'));
  state.currentItem.selectedSki = null;
  
  // Clear form fields
  document.getElementById('newSkiBrand').value = '';
  document.getElementById('newSkiModel').value = '';
  document.getElementById('newSkiLength').value = '';
  document.getElementById('newSkiCondition').value = 'USED';
  
  updateContinueButton();
}

function hideNewSkiForm() {
  document.getElementById('newSkiForm').style.display = 'none';
  document.getElementById('addNewSkiButtonContainer').style.display = 'block';
  
  // Clear form
  document.getElementById('newSkiBrand').value = '';
  document.getElementById('newSkiModel').value = '';
  document.getElementById('newSkiLength').value = '';
  document.getElementById('newSkiCondition').value = 'USED';
  
  // Clear the newSki state (user canceled)
  state.currentItem.newSki = null;
  
  // Remove any "new ski" visual cards
  document.querySelectorAll('.equipment-card.new-ski').forEach(card => card.remove());
  
  updateContinueButton();
}

function handleSaveNewSki() {
  const brand = document.getElementById('newSkiBrand').value.trim();
  const model = document.getElementById('newSkiModel').value.trim();
  const length = document.getElementById('newSkiLength').value.trim();
  const condition = document.getElementById('newSkiCondition').value;

  if (!brand || !model || !length) {
    showError('equipmentError', 'Please fill in all required ski fields.');
    return;
  }

  // Store new ski data in current item
  state.currentItem.newSki = {
    type: 'SKI',
    brand,
    model,
    length: parseInt(length),
    condition
  };

  // Clear existing ski selection
  state.currentItem.selectedSki = null;

  // Hide form and show the Add New Ski button again (DON'T clear the newSki data)
  document.getElementById('newSkiForm').style.display = 'none';
  document.getElementById('addNewSkiButtonContainer').style.display = 'block';
  
  // Show confirmation that ski was added
  const existingSkisSection = document.getElementById('existingSkisSection');
  const existingSkisList = document.getElementById('existingSkisList');
  
  // Remove any previous "new ski" confirmation cards
  document.querySelectorAll('.equipment-card.new-ski').forEach(card => card.remove());
  
  // Add a visual card showing the new ski that will be created
  const newSkiCard = document.createElement('div');
  newSkiCard.className = 'equipment-card new-ski selected';
  newSkiCard.innerHTML = `
    <div class="equipment-card-header">
      <div class="equipment-icon">⛷️</div>
      <div>
        <div class="equipment-card-title">${brand} ${model}</div>
        <div class="equipment-card-details">${length}cm</div>
      </div>
    </div>
    <span class="equipment-badge">${condition}</span>
    <span class="equipment-badge" style="background: #10b981; color: white;">New Equipment</span>
  `;
  
  // Show existing skis section if hidden
  existingSkisSection.style.display = 'block';
  
  // Clear other selections
  document.querySelectorAll('#existingSkisList .equipment-card').forEach(c => c.classList.remove('selected'));
  
  // Add to the list
  existingSkisList.appendChild(newSkiCard);

  // Enable continue button
  updateContinueButton();
}

// ==================== Continue Button Logic ====================
function handleContinueToService() {
  const hasSki = state.currentItem.selectedSki || state.currentItem.newSki;
  const hasEquipmentItems = state.equipmentItems.length > 0;
  
  if (hasSki) {
    // Ski selected - proceed to service selection (step 3)
    goToStep(3);
  } else if (hasEquipmentItems) {
    // No current ski but has equipment items - go directly to review (step 4)
    displayOrderSummary();
    goToStep(4);
  }
}

function updateContinueButton() {
  const continueBtn = document.getElementById('continueToStep3');
  
  // Enable if we have a ski selected OR we already have equipment items added
  const hasSki = state.currentItem.selectedSki || state.currentItem.newSki;
  const hasEquipmentItems = state.equipmentItems.length > 0;
  
  continueBtn.disabled = !(hasSki || hasEquipmentItems);
}

// ==================== Step 3: Service Selection ====================
function handleServiceFormSubmit(e) {
  e.preventDefault();
  hideError('serviceError');

  const serviceType = document.getElementById('serviceType').value;

  if (!serviceType) {
    showError('serviceError', 'Please select a service type.');
    return;
  }

  // Store service data in current item
  state.currentItem.serviceType = serviceType;

  // If service type is MOUNT, show boot selection
  if (serviceType === 'MOUNT') {
    showBootSelectionForMount();
  } else {
    // Clear boot selection for non-mount services
    state.currentItem.selectedBoot = null;
    state.currentItem.newBoot = null;
    
    // Save current item to equipment list
    saveCurrentItemToEquipmentList();
    
    // Show choice dialog for adding more equipment or reviewing order
    showEquipmentChoiceDialog();
  }
}

function showBootSelectionForMount() {
  const bootSection = document.getElementById('bootSelectionForMount');
  const existingBootsList = document.getElementById('mountBootsList');
  
  // Show the boot selection section
  bootSection.style.display = 'block';
  document.getElementById('serviceFormButtons').style.display = 'none';
  
  // Load boots
  existingBootsList.innerHTML = '';
  
  if (state.currentItem.selectedSki) {
    // Load boots associated with selected ski
    loadBootsForMount(state.currentItem.selectedSki.id);
  } else if (state.availableEquipment.existingBoots && state.availableEquipment.existingBoots.length > 0) {
    // Show customer's boots
    state.availableEquipment.existingBoots.forEach(boot => {
      const card = createBootCardForMount(boot);
      existingBootsList.appendChild(card);
    });
  } else {
    existingBootsList.innerHTML = '<p class="help-text">No existing boots found. Please add a new boot.</p>';
  }
}

async function loadBootsForMount(skiId) {
  const existingBootsList = document.getElementById('mountBootsList');
  existingBootsList.innerHTML = '<div class="loading"><div class="spinner"></div></div>';

  try {
    const response = await fetch(`${API_BASE}/api/public/workorders/equipment/${skiId}/boots`);
    
    if (!response.ok) {
      throw new Error('Failed to load boots');
    }

    const boots = await response.json();
    existingBootsList.innerHTML = '';

    if (boots.length > 0) {
      boots.forEach(boot => {
        const card = createBootCardForMount(boot);
        existingBootsList.appendChild(card);
      });
    } else if (state.availableEquipment.existingBoots && state.availableEquipment.existingBoots.length > 0) {
      // Fallback to customer's boots
      state.availableEquipment.existingBoots.forEach(boot => {
        const card = createBootCardForMount(boot);
        existingBootsList.appendChild(card);
      });
    } else {
      existingBootsList.innerHTML = '<p class="help-text">No boots found. Please add a new boot.</p>';
    }
  } catch (error) {
    console.error('Error loading boots:', error);
    existingBootsList.innerHTML = '<p class="help-text">Failed to load boots. Please add a new boot.</p>';
  }
}

function createBootCardForMount(boot) {
  const card = document.createElement('div');
  card.className = 'equipment-card';
  card.dataset.bootId = boot.id;

  card.innerHTML = `
    <div class="equipment-card-header">
      <div class="equipment-icon">👢</div>
      <div>
        <div class="equipment-card-title">${boot.brand} ${boot.model}</div>
        <div class="equipment-card-details">BSL: ${boot.bsl}mm</div>
      </div>
    </div>
  `;

  card.addEventListener('click', () => handleMountBootSelection(boot, card));

  return card;
}

function handleMountBootSelection(boot, cardElement) {
  // Clear previous selection
  document.querySelectorAll('#mountBootsList .equipment-card').forEach(c => c.classList.remove('selected'));
  
  // Mark as selected
  cardElement.classList.add('selected');
  
  // Store selection
  state.currentItem.selectedBoot = boot;
  state.currentItem.newBoot = null;

  // Hide new boot form if open
  document.getElementById('newMountBootForm').style.display = 'none';

  // Show binding information section
  document.getElementById('bindingInfoSection').style.display = 'block';

  // Enable continue button
  updateMountContinueButton();
}

function showNewMountBootForm() {
  document.getElementById('newMountBootForm').style.display = 'block';
  
  // Hide binding section until boot is saved
  document.getElementById('bindingInfoSection').style.display = 'none';
  document.getElementById('newBootProfileFields').style.display = 'none';
  
  // Clear any selected existing boot
  document.querySelectorAll('#mountBootsList .equipment-card').forEach(c => c.classList.remove('selected'));
  state.currentItem.selectedBoot = null;
  
  // Clear profile data from state (will be re-entered for new boot)
  state.currentItem.binding.heightInches = null;
  state.currentItem.binding.weight = null;
  state.currentItem.binding.age = null;
  state.currentItem.binding.abilityLevel = null;
  
  updateMountContinueButton();
}

function hideNewMountBootForm() {
  document.getElementById('newMountBootForm').style.display = 'none';
  
  // Clear form
  document.getElementById('newMountBootBrand').value = '';
  document.getElementById('newMountBootModel').value = '';
  document.getElementById('newMountBootSize').value = '';
  document.getElementById('newMountBootBSL').value = '';
  
  // Clear the newBoot state (user canceled)
  state.currentItem.newBoot = null;
  updateMountContinueButton();
}

function clearBindingForm() {
  // Clear binding fields
  if (document.getElementById('bindingBrand')) document.getElementById('bindingBrand').value = '';
  if (document.getElementById('bindingModel')) document.getElementById('bindingModel').value = '';
  
  // Clear profile fields
  if (document.getElementById('skierHeight')) document.getElementById('skierHeight').value = '';
  if (document.getElementById('skierWeight')) document.getElementById('skierWeight').value = '';
  if (document.getElementById('skierAge')) document.getElementById('skierAge').value = '';
  if (document.getElementById('skierAbility')) document.getElementById('skierAbility').value = '';
  
  // Clear state in currentItem
  state.currentItem.binding = {
    brand: '',
    model: '',
    heightInches: null,
    weight: null,
    age: null,
    abilityLevel: null
  };
}

function handleSaveNewMountBoot() {
  const brand = document.getElementById('newMountBootBrand').value.trim();
  const model = document.getElementById('newMountBootModel').value.trim();
  const size = document.getElementById('newMountBootSize').value.trim();
  const bsl = document.getElementById('newMountBootBSL').value.trim();

  if (!brand || !model || !size) {
    showError('serviceError', 'Please fill in all required boot fields.');
    return;
  }

  // Store new boot data in current item
  state.currentItem.newBoot = {
    brand,
    model,
    size: parseFloat(size),
    bsl: bsl ? parseInt(bsl) : null
  };

  // Clear existing boot selection and profile data
  state.currentItem.selectedBoot = null;
  state.currentItem.binding.heightInches = null;
  state.currentItem.binding.weight = null;
  state.currentItem.binding.age = null;
  state.currentItem.binding.abilityLevel = null;

  // Hide form (don't clear the newBoot data)
  document.getElementById('newMountBootForm').style.display = 'none';

  // Show binding information section
  document.getElementById('bindingInfoSection').style.display = 'block';
  // Show profile fields for NEW boot (user needs to enter this info)
  document.getElementById('newBootProfileFields').style.display = 'block';
  // Enable continue button
  updateMountContinueButton();
}

function updateMountContinueButton() {
  const continueBtn = document.getElementById('continueFromMountBoot');
  const hasBoot = state.currentItem.selectedBoot || state.currentItem.newBoot;
  continueBtn.disabled = !hasBoot;
}

function handleContinueFromMountBoot() {
  hideError('serviceError');
  
  // Validate boot selection
  const hasBoot = state.currentItem.selectedBoot || state.currentItem.newBoot;
  if (!hasBoot) {
    showError('serviceError', 'Please select or add a boot for mount service.');
    return;
  }

  // Validate binding fields
  const bindingBrand = document.getElementById('bindingBrand')?.value?.trim();
  const bindingModel = document.getElementById('bindingModel')?.value?.trim();
  
  if (!bindingBrand || !bindingModel) {
    showError('serviceError', 'Please enter binding make and model.');
    return;
  }

  // Validate profile fields if creating new boot
  if (state.currentItem.newBoot) {
    const height = document.getElementById('skierHeight')?.value;
    const weight = document.getElementById('skierWeight')?.value;
    const age = document.getElementById('skierAge')?.value;
    const ability = document.getElementById('skierAbility')?.value;

    if (!height || !weight || !age || !ability) {
      showError('serviceError', 'Please fill in all profile fields (height, weight, age, ability level) for your new boot.');
      return;
    }
  }
  
  // Capture binding information
  captureBindingInformation();
  
  // Save current item to equipment list
  saveCurrentItemToEquipmentList();
  
  // Show choice dialog for adding more equipment or reviewing order
  showEquipmentChoiceDialog();
}

function captureBindingInformation() {
  // Capture binding fields
  state.currentItem.binding.brand = document.getElementById('bindingBrand')?.value?.trim() || '';
  state.currentItem.binding.model = document.getElementById('bindingModel')?.value?.trim() || '';

  // Capture profile fields only if creating new boot (fields will be visible)
  if (state.currentItem.newBoot) {
    state.currentItem.binding.heightInches = document.getElementById('skierHeight')?.value ? parseInt(document.getElementById('skierHeight').value) : null;
    state.currentItem.binding.weight = document.getElementById('skierWeight')?.value ? parseInt(document.getElementById('skierWeight').value) : null;
    state.currentItem.binding.age = document.getElementById('skierAge')?.value ? parseInt(document.getElementById('skierAge').value) : null;
    state.currentItem.binding.abilityLevel = document.getElementById('skierAbility')?.value || null;
  }
  // If using existing boot, profile data was already set in handleMountBootSelection()
}

// ==================== Save Current Item to Equipment List ====================
function saveCurrentItemToEquipmentList() {
  // Create equipment item object
  const equipmentItem = {
    ski: state.currentItem.selectedSki || state.currentItem.newSki,
    serviceType: state.currentItem.serviceType,
    boot: state.currentItem.selectedBoot || state.currentItem.newBoot,
    binding: { ...state.currentItem.binding }
  };
  
  // Add to equipment items array
  state.equipmentItems.push(equipmentItem);
  
  // Reset current item for next equipment
  resetCurrentItem();
}

function resetCurrentItem() {
  state.currentItem = {
    selectedSki: null,
    selectedBoot: null,
    newSki: null,
   newBoot: null,
    serviceType: '',
    binding: {
      brand: '',
      model: '',
      heightInches: null,
      weight: null,
      age: null,
      abilityLevel: null
    }
  };
  
  // Clear ski selection UI
  document.querySelectorAll('#existingSkisList .equipment-card').forEach(c => c.classList.remove('selected'));
  document.querySelectorAll('.equipment-card.new-ski').forEach(card => card.remove());
  
  // Reset service form
  if (document.getElementById('serviceType')) {
    document.getElementById('serviceType').value = '';
  }
  
  // Clear binding form
  clearBindingForm();
}

// ==================== Equipment Choice Dialog ====================
function showEquipmentChoiceDialog() {
  // Hide service form and boot selection sections
  document.getElementById('serviceFormButtons').style.display = 'none';
  document.getElementById('bootSelectionForMount').style.display = 'none';
  document.getElementById('bindingInfoSection').style.display = 'none';
  
  // Show the choice dialog
  document.getElementById('equipmentChoiceDialog').style.display = 'block';
  
  // Scroll to dialog
  document.getElementById('equipmentChoiceDialog').scrollIntoView({ behavior: 'smooth', block: 'center' });
}

function handleAddAnotherEquipment() {
  // Hide the choice dialog
  document.getElementById('equipmentChoiceDialog').style.display = 'none';
  
  // Reset current item for next equipment entry
  resetCurrentItem();
  
  // Go back to step 2
  goToStep(2);
}

function handleProceedToReview() {
  // Hide the choice dialog
  document.getElementById('equipmentChoiceDialog').style.display = 'none';
  
  // Display summary
  displayOrderSummary();
  
  // Check if agreement step is needed (any MOUNT service)
  const needsAgreement = requiresMountAgreement();
  
  // Show/hide agreement step in progress bar
  const agreementStep = document.getElementById('agreementStep');
  const agreementConnector = document.getElementById('agreementConnector');
  if (agreementStep && agreementConnector) {
    if (needsAgreement) {
      agreementStep.style.display = 'flex';
      agreementConnector.style.display = 'block';
    } else {
      agreementStep.style.display = 'none';
      agreementConnector.style.display = 'none';
    }
  }
  
  if (needsAgreement) {
    // Show agreement step (step 5)
    goToStep(5);
  } else {
    // Skip agreement and go directly to review (step 4)
    goToStep(4);
  }
}

// Check if any equipment item requires mounting (has MOUNT service)
function requiresMountAgreement() {
  return state.equipmentItems.some(item => item.serviceType === 'MOUNT');
}

// ==================== Step 5: Agreement Signature (Conditional) ====================
let signatureCanvas, signatureContext, isDrawing = false, hasSignature = false;

function initializeSignaturePad() {
  signatureCanvas = document.getElementById('signatureCanvas');
  if (!signatureCanvas) return;
  
  signatureContext = signatureCanvas.getContext('2d');
  signatureContext.strokeStyle = '#000';
  signatureContext.lineWidth = 2;
  signatureContext.lineCap = 'round';
  
  // Mouse events
  signatureCanvas.addEventListener('mousedown', startDrawing);
  signatureCanvas.addEventListener('mousemove', draw);
  signatureCanvas.addEventListener('mouseup', stopDrawing);
  signatureCanvas.addEventListener('mouseleave', stopDrawing);
  
  // Touch events for mobile
  signatureCanvas.addEventListener('touchstart', handleTouchStart);
  signatureCanvas.addEventListener('touchmove', handleTouchMove);
  signatureCanvas.addEventListener('touchend', stopDrawing);
}

function handleAgreementScroll() {
  const scrollBox = document.getElementById('agreementScrollBox');
  const scrollWarning = document.getElementById('scrollWarning');
  const signatureSection = document.getElementById('signatureSection');
  
  // Check if scrolled to bottom (within 10px threshold)
  const scrolledToBottom = scrollBox.scrollHeight - scrollBox.scrollTop <= scrollBox.clientHeight + 10;
  
  if (scrolledToBottom && !state.agreement.scrolledToBottom) {
    state.agreement.scrolledToBottom = true;
    scrollWarning.style.display = 'none';
    signatureSection.style.opacity = '1';
    signatureSection.style.pointerEvents = 'auto';
    
    // Enable form controls
    document.getElementById('signatureName').disabled = false;
    document.getElementById('agreementCheckbox').disabled = false;
    document.getElementById('clearSignatureBtn').disabled = false;
    
    // Initialize signature pad if not already done
    if (!signatureCanvas) {
      initializeSignaturePad();
    }
    
    // Set expected name hint
    const expectedNameHint = document.getElementById('expectedNameHint');
    expectedNameHint.textContent = `Please enter: ${state.customer.name}`;
    
    // Restore from localStorage if exists
    restoreAgreementFromLocalStorage();
  }
}

function handleSignatureNameInput(e) {
  const typedName = e.target.value.trim();
  const expectedName = state.customer.name.trim();
  const errorElement = document.getElementById('nameValidationError');
  
  if (typedName && typedName.toLowerCase() !== expectedName.toLowerCase()) {
    errorElement.textContent = `Name must match: ${expectedName}`;
    errorElement.style.display = 'block';
  } else {
    errorElement.style.display = 'none';
  }
  
  validateAgreementForm();
  saveAgreementToLocalStorage();
}

function startDrawing(e) {
  if (!state.agreement.scrolledToBottom) return;
  isDrawing = true;
  const pos = getMousePos(e);
  signatureContext.beginPath();
  signatureContext.moveTo(pos.x, pos.y);
  hasSignature = true;
  document.getElementById('signaturePlaceholder').style.display = 'none';
}

function draw(e) {
  if (!isDrawing || !state.agreement.scrolledToBottom) return;
  e.preventDefault();
  const pos = getMousePos(e);
  signatureContext.lineTo(pos.x, pos.y);
  signatureContext.stroke();
  validateAgreementForm();
}

function stopDrawing() {
  if (isDrawing) {
    isDrawing = false;
    signatureContext.closePath();
    
    // Add has-signature class
    const signaturePad = document.getElementById('signaturePad');
    if (signaturePad && hasSignature) {
      signaturePad.classList.add('has-signature');
    }
    
    // Save to localStorage
    saveAgreementToLocalStorage();
  }
}

function handleTouchStart(e) {
  e.preventDefault();
  if (!state.agreement.scrolledToBottom) return;
  const touch = e.touches[0];
  const mouseEvent = new MouseEvent('mousedown', {
    clientX: touch.clientX,
    clientY: touch.clientY
  });
  signatureCanvas.dispatchEvent(mouseEvent);
}

function handleTouchMove(e) {
  e.preventDefault();
  if (!isDrawing) return;
  const touch = e.touches[0];
  const mouseEvent = new MouseEvent('mousemove', {
    clientX: touch.clientX,
    clientY: touch.clientY
  });
  signatureCanvas.dispatchEvent(mouseEvent);
}

function getMousePos(e) {
  const rect = signatureCanvas.getBoundingClientRect();
  return {
    x: e.clientX - rect.left,
    y: e.clientY - rect.top
  };
}

function clearSignature() {
  if (!signatureCanvas) return;
  signatureContext.clearRect(0, 0, signatureCanvas.width, signatureCanvas.height);
  hasSignature = false;
  document.getElementById('signaturePlaceholder').style.display = 'block';
  
  // Clear from localStorage
  localStorage.removeItem('agreementSignature');
  
  // Remove has-signature class
  const signaturePad = document.getElementById('signaturePad');
  if (signaturePad) {
    signaturePad.classList.remove('has-signature');
  }
  
  validateAgreementForm();
}

// ==================== Agreement localStorage Persistence ====================
function saveAgreementToLocalStorage() {
  const agreementData = {
    signature: hasSignature ? signatureCanvas.toDataURL('image/png') : null,
    signedName: document.getElementById('signatureName')?.value || '',
    checkboxChecked: document.getElementById('agreementCheckbox')?.checked || false,
    scrolledToBottom: state.agreement.scrolledToBottom
  };
  
  localStorage.setItem('agreementData', JSON.stringify(agreementData));
}

function restoreAgreementFromLocalStorage() {
  const savedData = localStorage.getItem('agreementData');
  if (!savedData) return;
  
  try {
    const agreementData = JSON.parse(savedData);
    
    // Restore signature if exists
    if (agreementData.signature && signatureCanvas) {
      const img = new Image();
      img.onload = function() {
        signatureContext.drawImage(img, 0, 0);
        hasSignature = true;
        document.getElementById('signaturePlaceholder').style.display = 'none';
        
        // Add has-signature class
        const signaturePad = document.getElementById('signaturePad');
        if (signaturePad) {
          signaturePad.classList.add('has-signature');
        }
        
        validateAgreementForm();
      };
      img.src = agreementData.signature;
    }
    
    // Restore signed name
    if (agreementData.signedName) {
      const nameInput = document.getElementById('signatureName');
      if (nameInput) {
        nameInput.value = agreementData.signedName;
      }
    }
    
    // Restore checkbox
    if (agreementData.checkboxChecked) {
      const checkbox = document.getElementById('agreementCheckbox');
      if (checkbox) {
        checkbox.checked = true;
        
        // Add checked class to label
        const checkboxLabel = document.getElementById('agreementCheckboxLabel');
        if (checkboxLabel) {
          checkboxLabel.classList.add('checked');
        }
      }
    }
    
    validateAgreementForm();
  } catch (error) {
    console.error('Error restoring agreement data:', error);
    localStorage.removeItem('agreementData');
  }
}

function clearAgreementFromLocalStorage() {
  localStorage.removeItem('agreementData');
}

function validateAgreementForm() {
  const typedName = document.getElementById('signatureName').value.trim();
  const expectedName = state.customer.name.trim();
  const isCheckboxChecked = document.getElementById('agreementCheckbox').checked;
  const continueBtn = document.getElementById('continueFromAgreement');
  const successIndicator = document.getElementById('agreementSuccessIndicator');
  
  // All validation rules must pass:
  // 1. Agreement fully scrolled
  // 2. Signature exists
  // 3. Typed name matches customer name (case insensitive)
  // 4. Agreement checkbox checked
  const isValid = 
    state.agreement.scrolledToBottom &&
    hasSignature &&
    typedName.toLowerCase() === expectedName.toLowerCase() &&
    isCheckboxChecked;
  
  continueBtn.disabled = !isValid;
  
  // Show/hide success indicator
  if (successIndicator) {
    successIndicator.style.display = isValid ? 'flex' : 'none';
  }
  
  return isValid;
}

function handleContinueFromAgreement() {
  hideError('agreementError');
  
  if (!validateAgreementForm()) {
    showError('agreementError', 'Please complete all required fields.');
    return;
  }
  
  // Save agreement data to state
  state.agreement.accepted = true;
  state.agreement.signedName = document.getElementById('signatureName').value.trim();
  state.agreement.signatureImageBase64 = signatureCanvas.toDataURL('image/png');
  state.agreement.acceptedAt = new Date().toISOString();
  
  // Show review step
  goToStep(4);
}

// ==================== Step 4: Order Summary ====================
function displayOrderSummary() {
  // Customer information
  const summaryCustomer = document.getElementById('summaryCustomer');
  summaryCustomer.innerHTML = `
    <div class="summary-item"><span class="summary-label">Name:</span> ${state.customer.name}</div>
    <div class="summary-item"><span class="summary-label">Email:</span> ${state.customer.email}</div>
    <div class="summary-item"><span class="summary-label">Phone:</span> ${state.customer.phone}</div>
  `;

  // Equipment information
  const summaryEquipment = document.getElementById('summaryEquipment');
  let equipmentHTML = '';

  // Display all equipment items
  if (state.equipmentItems.length === 0) {
    equipmentHTML = '<div class="summary-item" style="color: #64748b;">No equipment added yet</div>';
  } else {
    state.equipmentItems.forEach((item, index) => {
      equipmentHTML += `<div style="margin-bottom: 20px; padding-bottom: 20px; border-bottom: ${index < state.equipmentItems.length - 1 ? '2px solid #dee2e6' : 'none'};">`;
      equipmentHTML += `<strong style="color: #3b82f6; font-size: 16px;">Equipment ${index + 1}</strong><br><br>`;
      
      // Ski
      const ski = item.ski;
      const isNewSki = !ski.id;
      equipmentHTML += `<div class="summary-item"><span class="summary-label">Ski:</span> ${ski.brand} ${ski.model} (${ski.length}cm)${isNewSki ? ' <span style="color: #10b981; font-weight: 600;">- New</span>' : ''}</div>`;
      
      // Service Type
      equipmentHTML += `<div class="summary-item"><span class="summary-label">Service:</span> ${item.serviceType}</div>`;
      
      // Boot (if MOUNT)
      if (item.serviceType === 'MOUNT' && item.boot) {
        const boot = item.boot;
        const isNewBoot = !boot.id;
        if (isNewBoot) {
          equipmentHTML += `<div class="summary-item"><span class="summary-label">Boot:</span> ${boot.brand} ${boot.model} (Size: ${boot.size}) <span style="color: #10b981; font-weight: 600;">- New</span></div>`;
        } else {
          equipmentHTML += `<div class="summary-item"><span class="summary-label">Boot:</span> ${boot.brand} ${boot.model} (BSL: ${boot.bsl}mm)</div>`;
        }
        
        // Binding info
        if (item.binding && item.binding.brand) {
          equipmentHTML += `<div class="summary-item"><span class="summary-label">Binding:</span> ${item.binding.brand} ${item.binding.model || ''}</div>`;
        }
      }
      
      equipmentHTML += '</div>';
    });
  }

  summaryEquipment.innerHTML = equipmentHTML;

  // Service summary
  const summaryService = document.getElementById('summaryService');
  summaryService.innerHTML = `<div class="summary-item"><span class="summary-label">Total Equipment Items:</span> <strong>${state.equipmentItems.length}</strong></div>`;
}

// ==================== Submit Work Order ====================
async function handleSubmitWorkOrder() {
  hideError('submitError');

  const submitBtn = document.getElementById('submitWorkOrder');
  submitBtn.disabled = true;
  submitBtn.textContent = 'Submitting...';

  try {
    // Build the request payload
    const payload = buildWorkOrderPayload();

    // Submit to API
    const response = await fetch(`${API_BASE}/api/public/workorders`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(payload)
    });

    const data = await response.json();

    if (response.status === 429) {
      // Daily limit reached
      showLimitReachedScreen(data.message);
    } else if (!response.ok) {
      throw new Error(data.message || 'Failed to create work order');
    } else {
      // Success
      // Trigger agreement PDF workflow if agreement was accepted
      if (requiresMountAgreement() && state.agreement.accepted && data.workOrderId) {
        try {
          const agreementPayload = {
            signatureName: state.agreement.signedName,
            email: state.customer.email,
            phone: state.customer.phone,
            signatureImageBase64: state.agreement.signatureImageBase64
          };
          const agreementResponse = await fetch(`${API_BASE}/api/public/workorders/${data.workOrderId}/sign-agreement`, {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json'
            },
            body: JSON.stringify(agreementPayload)
          });
          const agreementData = await agreementResponse.json();
          if (!agreementResponse.ok) {
            console.error('Agreement PDF workflow failed:', agreementData.message || agreementData);
          } else {
            console.log('Agreement PDF workflow succeeded:', agreementData);
          }
        } catch (agreementError) {
          console.error('Error triggering agreement PDF workflow:', agreementError);
        }
      }
      showSuccessScreen(data.workOrderId);
    }

  } catch (error) {
    console.error('Error submitting work order:', error);
    showError('submitError', error.message || 'Failed to submit work order. Please try again.');
    submitBtn.disabled = false;
    submitBtn.textContent = 'Submit Work Order';
  }
}

function buildWorkOrderPayload() {
  // Parse customer name
  const nameParts = state.customer.name.split(' ');
  const firstName = nameParts[0];
  const lastName = nameParts.slice(1).join(' ') || firstName;

  const payload = {
    customerFirstName: firstName,
    customerLastName: lastName,
    email: state.customer.email,
    phone: state.customer.phone,
    equipment: []
  };

  // Add agreement data if MOUNT service exists
  if (requiresMountAgreement() && state.agreement.accepted) {
    payload.agreementAccepted = true;
    payload.agreementVersion = 'v1';
    payload.signedName = state.agreement.signedName;
    payload.signatureImageBase64 = state.agreement.signatureImageBase64;
    payload.agreementAcceptedAt = state.agreement.acceptedAt;
  }

  // Build equipment array from all items
  state.equipmentItems.forEach(item => {
    const equipmentItem = {
      serviceType: item.serviceType
    };

    // Add ski
    if (item.ski.id) {
      // Using existing ski
      equipmentItem.equipmentId = item.ski.id;
    } else {
      // Creating new ski
      equipmentItem.newEquipment = item.ski;
    }

    // Add boot information for MOUNT services
    if (item.serviceType === 'MOUNT' && item.boot) {
      if (item.boot.id) {
        equipmentItem.bootId = item.boot.id;
      } else {
        equipmentItem.newBoot = item.boot;
      }

      // Add binding information
      if (item.binding.brand) {
        equipmentItem.bindingBrand = item.binding.brand;
      }
      if (item.binding.model) {
        equipmentItem.bindingModel = item.binding.model;
      }

      // Add profile data
      if (item.binding.heightInches) {
        equipmentItem.heightInches = item.binding.heightInches;
      }
      if (item.binding.weight) {
        equipmentItem.weight = item.binding.weight;
      }
      if (item.binding.age) {
        equipmentItem.age = item.binding.age;
      }
      if (item.binding.abilityLevel) {
        equipmentItem.skiAbilityLevel = item.binding.abilityLevel;
      }
    }

    payload.equipment.push(equipmentItem);
  });

  return payload;
}

// ==================== Success & Error Screens ====================
function showSuccessScreen(workOrderId) {
  // Clear agreement localStorage data
  clearAgreementFromLocalStorage();
  
  // Hide all other steps
  document.querySelectorAll('.step-content').forEach(step => {
    step.style.display = 'none';
  });

  // Show success screen
  document.getElementById('successScreen').style.display = 'block';
  document.getElementById('workOrderNumber').textContent = `#${workOrderId}`;
  document.getElementById('workOrderNumberCopy').textContent = `#${workOrderId}`;

  // Update progress bar to show all completed
  document.querySelectorAll('.progress-step').forEach(step => {
    step.classList.remove('active');
    step.classList.add('completed');
  });

  // Scroll to top
  window.scrollTo({ top: 0, behavior: 'smooth' });
}

function showLimitReachedScreen(message) {
  // Hide all other steps
  document.querySelectorAll('.step-content').forEach(step => {
    step.style.display = 'none';
  });

  // Show limit reached screen
  document.getElementById('limitReachedScreen').style.display = 'block';
  
  if (message) {
    document.querySelector('.limit-message').textContent = message;
  }

  // Scroll to top
  window.scrollTo({ top: 0, behavior: 'smooth' });
}

// ==================== Utility Functions ====================
function formatPhoneNumber(phone) {
  // Remove all non-numeric characters
  const cleaned = phone.replace(/\D/g, '');
  
  // Format as (XXX) XXX-XXXX
  if (cleaned.length === 10) {
    return `(${cleaned.slice(0, 3)}) ${cleaned.slice(3, 6)}-${cleaned.slice(6)}`;
  }
  
  return phone;
}

// ==================== Debug Helper ====================
if (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1') {
  window.debugState = () => {
    console.log('Current State:', state);
  };
  console.log('Debug mode enabled. Call window.debugState() to view current state.');
}
