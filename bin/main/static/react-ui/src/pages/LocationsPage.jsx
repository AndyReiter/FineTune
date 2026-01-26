import React, { useEffect, useState } from 'react';
import { locationApi } from '../api/apiClient';

export default function LocationsPage() {
  const [locations, setLocations] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [formData, setFormData] = useState({ address: '', city: '', state: '', zipCode: '' });
  const [editingId, setEditingId] = useState(null);

  useEffect(() => {
    loadLocations();
  }, []);

  const loadLocations = async () => {
    try {
      setLoading(true);
      setError('');
      const data = await locationApi.getAll();
      setLocations(data);
    } catch (err) {
      setError('Failed to load locations: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (editingId) {
        await locationApi.update(editingId, formData);
      } else {
        await locationApi.create(formData);
      }
      setFormData({ address: '', city: '', state: '', zipCode: '' });
      setEditingId(null);
      setShowForm(false);
      loadLocations();
    } catch (err) {
      setError('Failed to save location: ' + err.message);
    }
  };

  const handleEdit = (location) => {
    setFormData(location);
    setEditingId(location.id);
    setShowForm(true);
  };

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure?')) {
      try {
        await locationApi.delete(id);
        loadLocations();
      } catch (err) {
        setError('Failed to delete location: ' + err.message);
      }
    }
  };

  return (
    <div className="p-8">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold">Locations</h1>
        <button
          onClick={() => {
            setShowForm(!showForm);
            setEditingId(null);
            setFormData({ address: '', city: '', state: '', zipCode: '' });
          }}
          className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
        >
          {showForm ? 'Cancel' : 'Add Location'}
        </button>
      </div>

      {error && <div className="bg-red-100 text-red-800 p-4 rounded mb-4">{error}</div>}

      {showForm && (
        <form onSubmit={handleSubmit} className="bg-white p-6 rounded shadow mb-6">
          <div className="grid grid-cols-1 gap-4">
            <input
              type="text"
              placeholder="Address"
              value={formData.address}
              onChange={(e) => setFormData({ ...formData, address: e.target.value })}
              className="border rounded px-4 py-2"
              required
            />
            <input
              type="text"
              placeholder="City"
              value={formData.city}
              onChange={(e) => setFormData({ ...formData, city: e.target.value })}
              className="border rounded px-4 py-2"
              required
            />
            <input
              type="text"
              placeholder="State"
              value={formData.state}
              onChange={(e) => setFormData({ ...formData, state: e.target.value })}
              className="border rounded px-4 py-2"
              required
            />
            <input
              type="text"
              placeholder="Zip Code"
              value={formData.zipCode}
              onChange={(e) => setFormData({ ...formData, zipCode: e.target.value })}
              className="border rounded px-4 py-2"
              required
            />
            <button
              type="submit"
              className="bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700"
            >
              {editingId ? 'Update' : 'Create'} Location
            </button>
          </div>
        </form>
      )}

      {loading ? (
        <p>Loading locations...</p>
      ) : (
        <div className="grid grid-cols-1 gap-4">
          {locations.map(location => (
            <div key={location.id} className="bg-white p-6 rounded shadow">
              <h3 className="text-lg font-bold mb-2">{location.address}</h3>
              <p className="text-gray-600 mb-2">{location.city}, {location.state} {location.zipCode}</p>
              <div className="flex gap-2">
                <button
                  onClick={() => handleEdit(location)}
                  className="bg-yellow-500 text-white px-3 py-1 rounded hover:bg-yellow-600"
                >
                  Edit
                </button>
                <button
                  onClick={() => handleDelete(location.id)}
                  className="bg-red-600 text-white px-3 py-1 rounded hover:bg-red-700"
                >
                  Delete
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
