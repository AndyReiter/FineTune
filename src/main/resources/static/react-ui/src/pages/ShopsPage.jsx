import React, { useEffect, useState } from 'react';
import { shopApi, locationApi } from '../api/apiClient';

export default function ShopsPage() {
  const [shops, setShops] = useState([]);
  const [locations, setLocations] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [formData, setFormData] = useState({ name: '', status: '', location: null });
  const [editingId, setEditingId] = useState(null);

  useEffect(() => {
    loadShops();
    loadLocations();
  }, []);

  const loadShops = async () => {
    try {
      setLoading(true);
      setError('');
      const data = await shopApi.getAll();
      setShops(data);
    } catch (err) {
      setError('Failed to load shops: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  const loadLocations = async () => {
    try {
      const data = await locationApi.getAll();
      setLocations(data);
    } catch (err) {
      console.error('Failed to load locations', err);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (editingId) {
        await shopApi.update(editingId, formData);
      } else {
        await shopApi.create(formData);
      }
      setFormData({ name: '', status: '', location: null });
      setEditingId(null);
      setShowForm(false);
      loadShops();
    } catch (err) {
      setError('Failed to save shop: ' + err.message);
    }
  };

  const handleEdit = (shop) => {
    setFormData(shop);
    setEditingId(shop.id);
    setShowForm(true);
  };

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure?')) {
      try {
        await shopApi.delete(id);
        loadShops();
      } catch (err) {
        setError('Failed to delete shop: ' + err.message);
      }
    }
  };

  return (
    <div className="p-8">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold">Shops</h1>
        <button
          onClick={() => {
            setShowForm(!showForm);
            setEditingId(null);
            setFormData({ name: '', status: '', location: null });
          }}
          className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
        >
          {showForm ? 'Cancel' : 'Add Shop'}
        </button>
      </div>

      {error && <div className="bg-red-100 text-red-800 p-4 rounded mb-4">{error}</div>}

      {showForm && (
        <form onSubmit={handleSubmit} className="bg-white p-6 rounded shadow mb-6">
          <div className="grid grid-cols-1 gap-4">
            <input
              type="text"
              placeholder="Shop Name"
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              className="border rounded px-4 py-2"
              required
            />
            <select
              value={formData.status}
              onChange={(e) => setFormData({ ...formData, status: e.target.value })}
              className="border rounded px-4 py-2"
              required
            >
              <option value="">Select Status</option>
              <option value="OPEN">Open</option>
              <option value="CLOSED">Closed</option>
            </select>
            <select
              value={formData.location?.id || ''}
              onChange={(e) => {
                const location = locations.find(l => l.id == e.target.value);
                setFormData({ ...formData, location });
              }}
              className="border rounded px-4 py-2"
            >
              <option value="">Select Location (Optional)</option>
              {locations.map(loc => (
                <option key={loc.id} value={loc.id}>
                  {loc.address}, {loc.city}, {loc.state}
                </option>
              ))}
            </select>
            <button
              type="submit"
              className="bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700"
            >
              {editingId ? 'Update' : 'Create'} Shop
            </button>
          </div>
        </form>
      )}

      {loading ? (
        <p>Loading shops...</p>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {shops.map(shop => (
            <div key={shop.id} className="bg-white p-6 rounded shadow">
              <h3 className="text-lg font-bold mb-2">{shop.name}</h3>
              <p className="text-gray-600 mb-2">Status: <span className="font-semibold">{shop.status}</span></p>
              {shop.location && (
                <p className="text-gray-600 mb-4">
                  Location: {shop.location.address}, {shop.location.city}, {shop.location.state}
                </p>
              )}
              <div className="flex gap-2">
                <button
                  onClick={() => handleEdit(shop)}
                  className="bg-yellow-500 text-white px-3 py-1 rounded hover:bg-yellow-600"
                >
                  Edit
                </button>
                <button
                  onClick={() => handleDelete(shop.id)}
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
