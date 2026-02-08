// src/components/WorkOrderForm.jsx
import React, { useState, useEffect } from 'react';
import { createWorkOrder, updateWorkOrder, getWorkOrderById } from '../services/api';
import { useNavigate, useParams } from 'react-router-dom';

const WorkOrderForm = ({ isEdit = false }) => {
  const { id } = useParams();
  const navigate = useNavigate();

  // Form state
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    phoneNumber: '',
  });

  const [loading, setLoading] = useState(isEdit);
  const [error, setError] = useState(null);

  // If editing, fetch existing work order
  useEffect(() => {
    if (isEdit) {
      const fetchWorkOrder = async () => {
        try {
          const data = await getWorkOrderById(id);
          setFormData({
            firstName: data.firstName || '',
            lastName: data.lastName || '',
            email: data.email || '',
            phoneNumber: data.phoneNumber || '',
          });
        } catch (err) {
          setError('Failed to load work order.');
        } finally {
          setLoading(false);
        }
      };
      fetchWorkOrder();
    }
  }, [id, isEdit]);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (isEdit) {
        await updateWorkOrder(id, formData);
      } else {
        await createWorkOrder(formData);
      }
      navigate('/workorders');
    } catch (err) {
      setError('Failed to save work order.');
    }
  };

  if (loading) return <div className="text-center mt-10">Loading...</div>;
  if (error) return <div className="text-center mt-10 text-red-500">{error}</div>;

  return (
    <div className="p-6 max-w-lg mx-auto">
      <h1 className="text-3xl font-bold mb-4">
        {isEdit ? 'Edit Work Order' : 'New Work Order'}
      </h1>
      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="block mb-1 font-semibold">First Name</label>
          <input
            type="text"
            name="firstName"
            value={formData.firstName}
            onChange={handleChange}
            className="w-full border px-3 py-2 rounded"
            required
          />
        </div>

        <div>
          <label className="block mb-1 font-semibold">Last Name</label>
          <input
            type="text"
            name="lastName"
            value={formData.lastName}
            onChange={handleChange}
            className="w-full border px-3 py-2 rounded"
            required
          />
        </div>

        <div>
          <label className="block mb-1 font-semibold">Email</label>
          <input
            type="email"
            name="email"
            value={formData.email}
            onChange={handleChange}
            className="w-full border px-3 py-2 rounded"
            required
          />
        </div>

        <div>
          <label className="block mb-1 font-semibold">Phone Number</label>
          <input
            type="tel"
            name="phoneNumber"
            value={formData.phoneNumber}
            onChange={(e) => {
              const numericValue = e.target.value.replace(/[^0-9]/g, '').slice(0, 10);
              setFormData({ ...formData, phoneNumber: numericValue });
            }}
            maxLength={10}
            placeholder="1234567890 (10 digits only)"
            className="w-full border px-3 py-2 rounded"
          />
        </div>

        <button
          type="submit"
          className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
        >
          Save
        </button>
      </form>
    </div>
  );
};

export default WorkOrderForm;
