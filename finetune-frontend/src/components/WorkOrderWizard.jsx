import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import CustomerSearch from './CustomerSearch';
import EquipmentSelection from './EquipmentSelection';
import { createWorkOrder } from '../services/api';

const WorkOrderWizard = () => {
  const navigate = useNavigate();
  const [step, setStep] = useState(1);
  const [error, setError] = useState(null);
  const [creating, setCreating] = useState(false);

  // Data collected during the wizard
  const [selectedCustomer, setSelectedCustomer] = useState(null);
  const [workOrderData, setWorkOrderData] = useState({});

  const handleCustomerSelected = (customer) => {
    setSelectedCustomer(customer);
    setStep(2);
  };

  const handleEquipmentComplete = async (equipmentData) => {
    setError(null);
    setCreating(true);

    try {
      // Build the complete work order payload
      const payload = {
        customerFirstName: selectedCustomer.firstName,
        customerLastName: selectedCustomer.lastName,
        email: selectedCustomer.email,
        phone: selectedCustomer.phone,
        equipment: equipmentData.skis
      };

      console.log('Creating work order with payload:', payload);

      // Create work order
      const result = await createWorkOrder(payload);
      
      console.log('Work order created successfully:', result);
      
      // Navigate back to dashboard
      navigate('/workorders', { 
        state: { message: 'Work order created successfully!' }
      });
      
    } catch (err) {
      console.error('Failed to create work order:', err);
      setError('Failed to create work order: ' + err.message);
      setCreating(false);
    }
  };

  const handleBack = () => {
    if (step > 1) {
      setStep(step - 1);
      setError(null);
    }
  };

  const handleCancel = () => {
    if (window.confirm('Are you sure you want to cancel? All data will be lost.')) {
      navigate('/workorders');
    }
  };

  return (
    <div className="max-w-4xl mx-auto p-6">
      {/* Header with progress indicator */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold mb-4">Create Work Order</h1>
        
        {/* Progress Steps */}
        <div className="flex items-center justify-between mb-6">
          <Step number={1} label="Customer" active={step === 1} completed={step > 1} />
          <StepConnector completed={step > 1} />
          <Step number={2} label="Equipment" active={step === 2} completed={step > 2} />
        </div>

        {/* Current customer info (if selected) */}
        {selectedCustomer && step > 1 && (
          <div className="bg-gray-50 border border-gray-200 p-4 rounded mb-4">
            <div className="flex justify-between items-center">
              <div>
                <p className="text-sm text-gray-600">Creating work order for:</p>
                <p className="font-semibold">
                  {selectedCustomer.firstName} {selectedCustomer.lastName}
                </p>
                <p className="text-sm text-gray-600">
                  {selectedCustomer.email} | {selectedCustomer.phone}
                </p>
              </div>
              {step === 2 && (
                <button
                  onClick={handleBack}
                  className="text-blue-600 hover:text-blue-800 text-sm"
                >
                  Change Customer
                </button>
              )}
            </div>
          </div>
        )}
      </div>

      {/* Error Display */}
      {error && (
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-6">
          <div className="flex justify-between items-center">
            <span>{error}</span>
            <button 
              onClick={() => setError(null)}
              className="text-red-700 hover:text-red-900 font-bold"
            >
              ✕
            </button>
          </div>
        </div>
      )}

      {/* Loading Overlay */}
      {creating && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white p-8 rounded-lg shadow-xl">
            <div className="text-center">
              <div className="animate-spin rounded-full h-16 w-16 border-b-2 border-blue-600 mx-auto mb-4"></div>
              <p className="text-lg font-semibold">Creating work order...</p>
            </div>
          </div>
        </div>
      )}

      {/* Step Content */}
      <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        {step === 1 && (
          <CustomerSearch onCustomerSelected={handleCustomerSelected} />
        )}

        {step === 2 && selectedCustomer && (
          <EquipmentSelection
            customer={selectedCustomer}
            workOrderData={workOrderData}
            onComplete={handleEquipmentComplete}
          />
        )}
      </div>

      {/* Footer Actions */}
      <div className="mt-6 flex justify-between">
        <button
          onClick={handleCancel}
          className="px-6 py-2 border border-gray-300 rounded hover:bg-gray-50"
        >
          Cancel
        </button>

        <div className="flex gap-3">
          {step > 1 && (
            <button
              onClick={handleBack}
              className="px-6 py-2 border border-gray-300 rounded hover:bg-gray-50"
              disabled={creating}
            >
              ← Back
            </button>
          )}
        </div>
      </div>
    </div>
  );
};

// Progress step component
const Step = ({ number, label, active, completed }) => {
  return (
    <div className="flex items-center">
      <div
        className={`w-10 h-10 rounded-full flex items-center justify-center font-bold text-sm ${
          completed
            ? 'bg-green-500 text-white'
            : active
            ? 'bg-blue-600 text-white'
            : 'bg-gray-300 text-gray-600'
        }`}
      >
        {completed ? '✓' : number}
      </div>
      <span
        className={`ml-2 font-medium ${
          active ? 'text-blue-600' : completed ? 'text-green-600' : 'text-gray-500'
        }`}
      >
        {label}
      </span>
    </div>
  );
};

const StepConnector = ({ completed }) => {
  return (
    <div
      className={`flex-1 h-1 mx-4 ${
        completed ? 'bg-green-500' : 'bg-gray-300'
      }`}
    />
  );
};

export default WorkOrderWizard;
