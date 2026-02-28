const API_URL = '/shops';
const LOCATIONS_API_URL = '/locations';

let form, shopIdInput, shopNameInput, shopStatusInput, shopAddressInput, shopCityInput, shopStateInput, shopZipCodeInput;
let messageElement, shopsListElement, refreshBtn, errorModal, errorMessageElement;

let currentEditId = null;

// Load shops on page load
document.addEventListener('DOMContentLoaded', () => {
  // Initialize all DOM elements
  form = document.getElementById('shopForm');
  shopIdInput = document.getElementById('shopId');
  shopNameInput = document.getElementById('shopName');
  shopStatusInput = document.getElementById('shopStatus');
  shopAddressInput = document.getElementById('shopAddress');
  shopCityInput = document.getElementById('shopCity');
  shopStateInput = document.getElementById('shopState');
  shopZipCodeInput = document.getElementById('shopZipCode');
  messageElement = document.getElementById('shopMessage');
  shopsListElement = document.getElementById('shopsList');
  refreshBtn = document.getElementById('refreshShopsBtn');
  errorModal = document.getElementById('errorModal');
  errorMessageElement = document.getElementById('errorMessage');
  
  console.log('DOM elements initialized:', {
    form: !!form,
    errorModal: !!errorModal,
    errorMessageElement: !!errorMessageElement
  });

  // Handle form submission
  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    
    try {
      // Validate that location fields are filled
      if (!shopAddressInput.value.trim() || !shopCityInput.value.trim() || !shopStateInput.value.trim()) {
        showMessage('Please fill in Address, City, and State fields.', 'error');
        return;
      }

      // Prepare location data
      const locationData = {
        name: shopNameInput.value || 'Location',
        address: shopAddressInput.value,
        city: shopCityInput.value,
        state: shopStateInput.value,
        zipCode: shopZipCodeInput.value
      };

      let location;
      if (currentEditId) {
        // When editing shop, we need to get the existing location ID
        const existingShop = await (await APIUtils.authenticatedFetch(`${API_URL}/${currentEditId}`)).json();
        if (existingShop.location && existingShop.location.id) {
          // Update existing location
          location = await updateExistingLocation(existingShop.location.id, locationData);
        } else {
          // Create new location if shop doesn't have one
          location = await createOrUpdateLocation(locationData);
        }
      } else {
        // Create new location for new shop
        location = await createOrUpdateLocation(locationData);
      }

      if (!location || !location.id) {
        throw new Error('Location operation failed - no ID returned');
      }

      // Prepare shop data
      const shopData = {
        name: shopNameInput.value,
        status: shopStatusInput.value,
        location: { id: location.id }
      };

      if (currentEditId) {
        // Update existing shop
        shopData.id = currentEditId;
        await updateShop(currentEditId, shopData);
      } else {
        // Create new shop
        await createShop(shopData);
      }
      
      form.reset();
      shopIdInput.value = '';
      currentEditId = null;
      loadShops();
    } catch (error) {
      // Check if it's a location-related error
      console.log('Error caught:', error.message);
      const errorMsg = error.message.toLowerCase();
      
      if (errorMsg.includes('location') || errorMsg.includes('address')) {
        // Show pretty error modal for location-related errors
        showErrorModal(error.message);
      } else {
        showMessage('Error saving shop: ' + error.message, 'error');
      }
      console.error('Form submission error:', error);
    }
  });

  // Refresh button
  refreshBtn.addEventListener('click', loadShops);

  loadShops();
});

// Create new location
async function createOrUpdateLocation(locationData) {
  try {
    console.log('Creating location with data:', locationData);
    
    const response = await APIUtils.authenticatedFetch(LOCATIONS_API_URL, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(locationData)
    });

    if (!response.ok) {
      const errorText = await response.text();
      console.error('Location creation failed with status:', response.status);
      console.error('Error response text:', errorText);
      
      // Extract error message from response
      let errorMessage = 'Failed to create location: ' + response.statusText;
      try {
        const errorJson = JSON.parse(errorText);
        if (errorJson.message) {
          errorMessage = errorJson.message;
        } else if (typeof errorJson === 'string') {
          errorMessage = errorJson;
        }
      } catch (e) {
        // If response is plain text, use it as is
        if (errorText) {
          errorMessage = errorText;
        }
      }
      
      console.log('Final error message:', errorMessage);
      throw new Error(errorMessage);
    }

    const location = await response.json();
    console.log('Location created successfully:', location);
    return location;
  } catch (error) {
    console.error('Error creating location:', error);
    throw error;
  }
}

// Update existing location
async function updateExistingLocation(locationId, locationData) {
  try {
    console.log('Updating location', locationId, 'with data:', locationData);
    
    const response = await APIUtils.authenticatedFetch(`${LOCATIONS_API_URL}/${locationId}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(locationData)
    });

    if (!response.ok) {
      const errorText = await response.text();
      console.error('Location update failed with status:', response.status);
      console.error('Error response text:', errorText);
      
      // Extract error message from response
      let errorMessage = 'Failed to update location: ' + response.statusText;
      try {
        const errorJson = JSON.parse(errorText);
        if (errorJson.message) {
          errorMessage = errorJson.message;
        } else if (typeof errorJson === 'string') {
          errorMessage = errorJson;
        }
      } catch (e) {
        // If response is plain text, use it as is
        if (errorText) {
          errorMessage = errorText;
        }
      }
      
      console.log('Final error message:', errorMessage);
      throw new Error(errorMessage);
    }

    const location = await response.json();
    console.log('Location updated successfully:', location);
    return location;
  } catch (error) {
    console.error('Error updating location:', error);
    throw error;
  }
}

// Fetch all shops
async function loadShops() {
  try {
    const response = await APIUtils.authenticatedFetch(API_URL);
    if (!response.ok) {
      throw new Error('Failed to fetch shops');
    }
    const shops = await response.json();
    displayShops(shops);
    showMessage('Shops loaded successfully', 'success');
  } catch (error) {
    showMessage('Error loading shops: ' + error.message, 'error');
    shopsListElement.innerHTML = '<p class="error">Failed to load shops</p>';
  }
}

// Display shops in the list
function displayShops(shops) {
  if (shops.length === 0) {
    shopsListElement.innerHTML = '<p>No shops found. Create one to get started!</p>';
    return;
  }

  shopsListElement.innerHTML = shops.map(shop => {
    const loc = shop.location;
    const address = loc ? `${loc.address}, ${loc.city}, ${loc.state} ${loc.zipCode}` : 'No location assigned';
    return `
    <div class="shop-card">
      <div class="shop-info">
        <h3>${shop.name}</h3>
        <p><strong>ID:</strong> ${shop.id}</p>
        <p><strong>Status:</strong> <span class="status-badge status-${shop.status.toLowerCase()}">${shop.status}</span></p>
        <p><strong>Address:</strong> ${address}</p>
      </div>
      <div class="shop-actions">
        <button class="btn btn-edit" onclick="editShop(${shop.id})">Edit</button>
        <button class="btn btn-delete" onclick="deleteShop(${shop.id})">Delete</button>
      </div>
    </div>
  `;
  }).join('');
}

// Create a new shop
async function createShop(shopData) {
  const response = await APIUtils.authenticatedFetch(API_URL, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(shopData)
  });

  if (!response.ok) {
    throw new Error('Failed to create shop');
  }

  showMessage('Shop created successfully!', 'success');
}

// Update an existing shop
async function updateShop(id, shopData) {
  const response = await APIUtils.authenticatedFetch(`${API_URL}/${id}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(shopData)
  });

  if (!response.ok) {
    throw new Error('Failed to update shop');
  }

  showMessage('Shop updated successfully!', 'success');
}

// Edit shop - fetch and populate form
async function editShop(id) {
  try {
    const response = await APIUtils.authenticatedFetch(`${API_URL}/${id}`);
    if (!response.ok) {
      throw new Error('Failed to fetch shop');
    }

    const shop = await response.json();
    currentEditId = id;
    shopIdInput.value = shop.id;
    shopNameInput.value = shop.name;
    shopStatusInput.value = shop.status;

    if (shop.location) {
      shopAddressInput.value = shop.location.address || '';
      shopCityInput.value = shop.location.city || '';
      shopStateInput.value = shop.location.state || '';
      shopZipCodeInput.value = shop.location.zipCode || '';
    }

    shopNameInput.focus();
  } catch (error) {
    showMessage('Error loading shop: ' + error.message, 'error');
  }
}

// Delete a shop
async function deleteShop(id) {
  if (!confirm('Are you sure you want to delete this shop?')) {
    return;
  }

  try {
    const response = await APIUtils.authenticatedFetch(`${API_URL}/${id}`, {
      method: 'DELETE'
    });

    if (!response.ok) {
      throw new Error('Failed to delete shop');
    }

    showMessage('Shop deleted successfully!', 'success');
    loadShops();
  } catch (error) {
    showMessage('Error deleting shop: ' + error.message, 'error');
  }
}

// Show temporary message
function showMessage(message, type = 'info') {
  messageElement.textContent = message;
  messageElement.className = type;
  
  setTimeout(() => {
    messageElement.textContent = '';
    messageElement.className = '';
  }, 3000);
}

// Show error modal for location/shop issues
function showErrorModal(message) {
  console.log('Showing error modal with message:', message);
  console.log('errorModal element:', errorModal);
  console.log('errorMessageElement:', errorMessageElement);
  
  const errorTitle = document.getElementById('errorTitle');
  const errorDescription = document.getElementById('errorDescription');
  const messageLower = message.toLowerCase();
  
  // Determine title and description based on error type
  let title = '⚠️ Error';
  let description = 'Please check your information and try again.';
  
  if (messageLower.includes('already exists')) {
    title = '⚠️ Duplicate Location';
    description = 'A shop already exists at this location. Please use a different address or edit the existing shop.';
  } else if (messageLower.includes('failed to create')) {
    title = '⚠️ Creation Failed';
    description = 'There was an issue creating the location. Please check your information and try again.';
  } else if (messageLower.includes('failed to update')) {
    title = '⚠️ Update Failed';
    description = 'There was an issue updating the location. Please check your information and try again.';
  }
  
  if (errorTitle) {
    errorTitle.textContent = title;
  }
  if (errorDescription) {
    errorDescription.textContent = description;
  }
  if (errorMessageElement) {
    errorMessageElement.textContent = message;
  }
  if (errorModal) {
    errorModal.classList.add('show');
    console.log('Modal classes after adding show:', errorModal.className);
  } else {
    console.error('Error modal element not found!');
  }
}

// Close error modal
function closeErrorModal() {
  errorModal.classList.remove('show');
}

// Close modal when clicking outside of it
window.addEventListener('click', (event) => {
  if (event.target === errorModal) {
    closeErrorModal();
  }
});

