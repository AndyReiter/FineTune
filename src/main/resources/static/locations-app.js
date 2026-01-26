const LOCATIONS_API_URL = '/locations';
const locationForm = document.getElementById('locationForm');
const locationIdInput = document.getElementById('locationId');
const locationNameInput = document.getElementById('locationName');
const locationAddressInput = document.getElementById('locationAddress');
const locationCityInput = document.getElementById('locationCity');
const locationStateInput = document.getElementById('locationState');
const locationZipCodeInput = document.getElementById('locationZipCode');
const locationMessageElement = document.getElementById('locationMessage');
const locationsListElement = document.getElementById('locationsList');
const refreshLocationsBtn = document.getElementById('refreshLocationsBtn');

let currentEditLocationId = null;

// Load locations on page load
document.addEventListener('DOMContentLoaded', () => {
  loadLocations();
});

// Handle form submission
locationForm.addEventListener('submit', async (e) => {
  e.preventDefault();
  
  const locationData = {
    name: locationNameInput.value,
    address: locationAddressInput.value,
    city: locationCityInput.value,
    state: locationStateInput.value,
    zipCode: locationZipCodeInput.value
  };

  try {
    if (currentEditLocationId) {
      // Update existing location
      locationData.id = currentEditLocationId;
      await updateLocation(currentEditLocationId, locationData);
    } else {
      // Create new location
      await createLocation(locationData);
    }
    locationForm.reset();
    locationIdInput.value = '';
    currentEditLocationId = null;
    loadLocations();
  } catch (error) {
    showLocationMessage('Error saving location: ' + error.message, 'error');
  }
});

// Refresh button
refreshLocationsBtn.addEventListener('click', loadLocations);

// Fetch all locations
async function loadLocations() {
  try {
    const response = await fetch(LOCATIONS_API_URL);
    if (!response.ok) {
      throw new Error('Failed to fetch locations');
    }
    
    const locations = await response.json();
    displayLocations(locations);
    showLocationMessage('Locations loaded successfully', 'success');
  } catch (error) {
    showLocationMessage('Error loading locations: ' + error.message, 'error');
    locationsListElement.innerHTML = '<p class="error">Failed to load locations</p>';
  }
}

// Display locations in the list
function displayLocations(locations) {
  if (locations.length === 0) {
    locationsListElement.innerHTML = '<p>No locations found. Create one to get started!</p>';
    return;
  }

  locationsListElement.innerHTML = locations.map(location => `
    <div class="location-card">
      <div class="location-info">
        <h3>${location.name}</h3>
        <p><strong>ID:</strong> ${location.id}</p>
        <p><strong>Address:</strong> ${location.address}</p>
        <p><strong>City:</strong> ${location.city}, ${location.state} ${location.zipCode}</p>
      </div>
      <div class="location-actions">
        <button class="btn btn-edit" onclick="editLocation(${location.id}, '${location.name}', '${location.address}', '${location.city}', '${location.state}', '${location.zipCode}')">Edit</button>
        <button class="btn btn-delete" onclick="deleteLocation(${location.id})">Delete</button>
      </div>
    </div>
  `).join('');
}

// Create a new location
async function createLocation(locationData) {
  const response = await fetch(LOCATIONS_API_URL, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(locationData)
  });

  if (!response.ok) {
    throw new Error('Failed to create location');
  }

  showLocationMessage('Location created successfully!', 'success');
}

// Update an existing location
async function updateLocation(id, locationData) {
  const response = await fetch(`${LOCATIONS_API_URL}/${id}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(locationData)
  });

  if (!response.ok) {
    throw new Error('Failed to update location');
  }

  showLocationMessage('Location updated successfully!', 'success');
}

// Edit location - populate form
function editLocation(id, name, address, city, state, zipCode) {
  currentEditLocationId = id;
  locationIdInput.value = id;
  locationNameInput.value = name;
  locationAddressInput.value = address;
  locationCityInput.value = city;
  locationStateInput.value = state;
  locationZipCodeInput.value = zipCode;
  locationNameInput.focus();
}

// Delete a location
async function deleteLocation(id) {
  if (!confirm('Are you sure you want to delete this location?')) {
    return;
  }

  try {
    const response = await fetch(`${LOCATIONS_API_URL}/${id}`, {
      method: 'DELETE'
    });

    if (!response.ok) {
      throw new Error('Failed to delete location');
    }

    showLocationMessage('Location deleted successfully!', 'success');
    loadLocations();
  } catch (error) {
    showLocationMessage('Error deleting location: ' + error.message, 'error');
  }
}

// Show temporary message
function showLocationMessage(message, type = 'info') {
  locationMessageElement.textContent = message;
  locationMessageElement.className = type;
  
  setTimeout(() => {
    locationMessageElement.textContent = '';
    locationMessageElement.className = '';
  }, 3000);
}
