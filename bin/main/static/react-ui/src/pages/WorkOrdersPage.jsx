import React, { useEffect, useState } from 'react';
import { workOrderApi } from '../api/apiClient';

export default function WorkOrdersPage() {
  const [workOrders, setWorkOrders] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [formData, setFormData] = useState({ customerName: '', phone: '', email: '' });
  const [editingId, setEditingId] = useState(null);
  const [successMessage, setSuccessMessage] = useState('');

  useEffect(() => {
    loadWorkOrders();
  }, []);

  const loadWorkOrders = async () => {
    try {
      setLoading(true);
      setError('');
      const data = await workOrderApi.getAll();
      setWorkOrders(data);
    } catch (err) {
      setError('Failed to load work orders: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      setError('');
      setSuccessMessage('');
      
      // Validate email
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!emailRegex.test(formData.email)) {
        setError('Please enter a valid email address');
        return;
      }

      // Validate phone (basic)
      if (formData.phone.length < 10) {
        setError('Please enter a valid phone number');
        return;
      }

      if (editingId) {
        await workOrderApi.update(editingId, formData);
        setSuccessMessage('Work order updated successfully!');
      } else {
        await workOrderApi.create(formData);
        setSuccessMessage('Work order created successfully!');
      }
      
      setFormData({ customerName: '', phone: '', email: '', status: 'RECEIVED' });
      setEditingId(null);
      setShowForm(false);
      loadWorkOrders();
      
      // Clear success message after 3 seconds
      setTimeout(() => setSuccessMessage(''), 3000);
    } catch (err) {
      setError('Failed to save work order: ' + err.message);
    }
  };

  const handleEdit = (workOrder) => {
    setFormData({
      customerName: workOrder.customerName,
      phone: workOrder.phone,
      email: workOrder.email,
      status: workOrder.status,
    });
    setEditingId(workOrder.id);
    setShowForm(true);
  };

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to delete this work order?')) {
      try {
        await workOrderApi.delete(id);
        setSuccessMessage('Work order deleted successfully!');
        loadWorkOrders();
        setTimeout(() => setSuccessMessage(''), 3000);
      } catch (err) {
        setError('Failed to delete work order: ' + err.message);
      }
    }
  };

  const getStatusColor = (status) => {
    switch(status) {
      case 'RECEIVED':
        return 'bg-blue-100 text-blue-800';
      case 'IN_PROGRESS':
        return 'bg-yellow-100 text-yellow-800';
      case 'READY_FOR_PICKUP':
        return 'bg-purple-100 text-purple-800';
      case 'COMPLETED':
        return 'bg-green-100 text-green-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  return (
    <div className="p-8">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold">Work Orders</h1>
        <button
          onClick={() => {
            setShowForm(!showForm);
            setEditingId(null);
            setFormData({ customerName: '', phone: '', email: '' });
            setError('');
          }}
          className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
        >
          {showForm ? 'Cancel' : 'New Work Order'}
        </button>
      </div>

      {error && <div className="bg-red-100 text-red-800 p-4 rounded mb-4">{error}</div>}
      {successMessage && <div className="bg-green-100 text-green-800 p-4 rounded mb-4">{successMessage}</div>}

      {showForm && (
        <form onSubmit={handleSubmit} className="bg-white p-6 rounded shadow mb-6">
          <h2 className="text-2xl font-bold mb-4">{editingId ? 'Edit Work Order' : 'Create New Work Order'}</h2>
          <div className="grid grid-cols-1 gap-4">
            <div>
              <label className="block text-sm font-semibold mb-1">Customer Name *</label>
              <input
                type="text"
                placeholder="John Doe"
                value={formData.customerName}
                onChange={(e) => setFormData({ ...formData, customerName: e.target.value })}
                className="w-full border rounded px-4 py-2"
                required
              />
            </div>
            <div>
              <label className="block text-sm font-semibold mb-1">Phone Number *</label>
              <input
                type="tel"
                placeholder="1234567890 (10 digits only)"
                value={formData.phone}
                onChange={(e) => {
                  const numericValue = e.target.value.replace(/[^0-9]/g, '').slice(0, 10);
                  setFormData({ ...formData, phone: numericValue });
                }}
                maxLength={10}
                className="w-full border rounded px-4 py-2"
                required
              />
            </div>
            <div>
              <label className="block text-sm font-semibold mb-1">Email *</label>
              <input
                type="email"
                placeholder="john@example.com"
                value={formData.email}
                onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                className="w-full border rounded px-4 py-2"
                required
              />
            </div>
            {/* Work Order Status is automatically calculated from item statuses */}
            <div className="text-sm text-gray-600 italic">
              Work order status will be automatically calculated based on item progress.
              <br />Initial status: RECEIVED (all items start as PENDING)
            </div>
            <button
              type="submit"
              className="bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700 font-semibold"
            >
              {editingId ? 'Update' : 'Create'} Work Order
            </button>
          </div>
        </form>
      )}

      {loading ? (
        <p className="text-center text-gray-600">Loading work orders...</p>
      ) : workOrders.length === 0 ? (
        <div className="bg-white p-8 rounded shadow text-center">
          <p className="text-gray-500 text-lg">No work orders yet. Create one to get started!</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 gap-4">
          {workOrders.map(workOrder => (
            <div key={workOrder.id} className="bg-white p-6 rounded shadow hover:shadow-lg transition">
              <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-4">
                <div>
                  <p className="text-gray-600 text-sm font-semibold">Customer Name</p>
                  <p className="text-lg font-bold">{workOrder.customerName}</p>
                </div>
                <div>
                  <p className="text-gray-600 text-sm font-semibold">Phone</p>
                  <a href={`tel:${workOrder.phone}`} className="text-blue-600 hover:underline">{workOrder.phone}</a>
                </div>
                <div>
                  <p className="text-gray-600 text-sm font-semibold">Email</p>
                  <a href={`mailto:${workOrder.email}`} className="text-blue-600 hover:underline">{workOrder.email}</a>
                </div>
                <div>
                  <p className="text-gray-600 text-sm font-semibold">Status</p>
                  <span className={`inline-block px-3 py-1 rounded text-sm font-semibold ${getStatusColor(workOrder.status)}`}>
                    {workOrder.status.replace(/_/g, ' ')}
                  </span>
                </div>
              </div>
              {workOrder.createdAt && (
                <p className="text-gray-500 text-sm mb-4">
                  Created: {new Date(workOrder.createdAt).toLocaleString()}
                </p>
              )}
              <div className="flex gap-2">
                <button
                  onClick={() => handleEdit(workOrder)}
                  className="bg-yellow-500 text-white px-4 py-2 rounded hover:bg-yellow-600 font-semibold"
                >
                  Edit
                </button>
                <button
                  onClick={() => handleDelete(workOrder.id)}
                  className="bg-red-600 text-white px-4 py-2 rounded hover:bg-red-700 font-semibold"
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
