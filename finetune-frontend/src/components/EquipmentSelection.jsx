import React, { useState, useEffect } from 'react';
import { fetchCustomerEquipment, fetchCustomerBoots, fetchSkiModels } from '../services/api';

const EquipmentSelection = ({ customer, workOrderData, onComplete }) => {
  const [equipmentList, setEquipmentList] = useState([]);
  const [bootsList, setBootsList] = useState([]);
  const [skiModels, setSkiModels] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Equipment item state
  const [equipmentItems, setEquipmentItems] = useState([{
    id: Date.now(),
    equipmentId: null,
    newEquipment: null,
    serviceType: '',
    useExisting: false,
    // Mount-specific fields
    bootId: null,
    bindingBrand: '',
    bindingModel: '',
    // New boot fields
    bootBrand: '',
    bootModel: '',
    bsl: '',
    heightFeet: '',
    heightInches: '',
    weight: '',
    age: '',
    abilityLevel: ''
  }]);

  useEffect(() => {
    loadData();
  }, [customer]);

  const loadData = async () => {
    try {
      setLoading(true);
      
      // Load customer equipment
      const equipment = await fetchCustomerEquipment(customer.id);
      setEquipmentList(equipment || []);
      
      // Load customer boots
      const boots = await fetchCustomerBoots(customer.email, customer.phone);
      setBootsList(boots || []);
      
      // Load ski models
      try {
        const models = await fetchSkiModels();
        setSkiModels(models || []);
      } catch (err) {
        console.log('Ski models not available');
        setSkiModels([]);
      }
      
    } catch (err) {
      setError('Failed to load equipment data');
    } finally {
      setLoading(false);
    }
  };

  const addEquipmentItem = () => {
    setEquipmentItems([...equipmentItems, {
      id: Date.now(),
      equipmentId: null,
      newEquipment: null,
      serviceType: '',
      useExisting: false,
      bootId: null,
      bindingBrand: '',
      bindingModel: '',
      bootBrand: '',
      bootModel: '',
      bsl: '',
      heightFeet: '',
      heightInches: '',
      weight: '',
      age: '',
      abilityLevel: ''
    }]);
  };

  const removeEquipmentItem = (id) => {
    if (equipmentItems.length > 1) {
      setEquipmentItems(equipmentItems.filter(item => item.id !== id));
    }
  };

  const updateEquipmentItem = (id, updates) => {
    setEquipmentItems(equipmentItems.map(item => 
      item.id === id ? { ...item, ...updates } : item
    ));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    setError(null);

    // Validate equipment items
    for (const item of equipmentItems) {
      if (!item.serviceType) {
        setError('Please select a service type for all equipment items');
        return;
      }

      // Check equipment selection
      if (!item.useExisting && !item.newEquipment) {
        setError('Please select existing equipment or create new equipment for all items');
        return;
      }

      // Validate mount service
      if (item.serviceType === 'MOUNT') {
        if (!item.bindingBrand) {
          setError('Binding brand is required for mount services');
          return;
        }

        // Boot validation
        if (!item.bootId) {
          // New boot - validate fields
          if (!item.bootBrand || !item.bootModel || !item.bsl || 
              !item.heightFeet || !item.heightInches || !item.weight || 
              !item.age || !item.abilityLevel) {
            setError('All boot information is required for mount services');
            return;
          }
        }
      }
    }

    // Build payload
    const equipment = equipmentItems.map(item => {
      const equipmentItem = {
        serviceType: item.serviceType
      };

      // Equipment selection
      if (item.useExisting) {
        equipmentItem.equipmentId = item.equipmentId;
      } else {
        equipmentItem.newEquipment = {
          brand: item.newEquipment.brand,
          model: item.newEquipment.model,
          condition: item.newEquipment.condition,
          abilityLevel: item.newEquipment.abilityLevel,
          length: item.newEquipment.length || null
        };
      }

      // Mount-specific data
      if (item.serviceType === 'MOUNT') {
        equipmentItem.bindingBrand = item.bindingBrand;
        equipmentItem.bindingModel = item.bindingModel || null;

        if (item.bootId) {
          equipmentItem.bootId = item.bootId;
        } else {
          // New boot
          equipmentItem.bootBrand = item.bootBrand;
          equipmentItem.bootModel = item.bootModel;
          equipmentItem.bsl = parseInt(item.bsl);
          equipmentItem.heightInches = (parseInt(item.heightFeet) * 12) + parseInt(item.heightInches);
          equipmentItem.weight = parseInt(item.weight);
          equipmentItem.age = parseInt(item.age);
          equipmentItem.skiAbilityLevel = item.abilityLevel;
        }
      }

      return equipmentItem;
    });

    onComplete({ skis: equipment });
  };

  if (loading) {
    return <div className="text-center py-10">Loading equipment data...</div>;
  }

  return (
    <div className="space-y-6">
      <h2 className="text-2xl font-bold">Step 3: Equipment & Service Selection</h2>
      
      <div className="bg-blue-50 border border-blue-200 p-4 rounded">
        <p><strong>Customer:</strong> {customer.firstName} {customer.lastName}</p>
        <p><strong>Email:</strong> {customer.email}</p>
      </div>

      {error && (
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-6">
        {equipmentItems.map((item, index) => (
          <EquipmentItemForm
            key={item.id}
            item={item}
            index={index}
            equipmentList={equipmentList}
            bootsList={bootsList}
            skiModels={skiModels}
            onUpdate={(updates) => updateEquipmentItem(item.id, updates)}
            onRemove={() => removeEquipmentItem(item.id)}
            canRemove={equipmentItems.length > 1}
          />
        ))}

        <button
          type="button"
          onClick={addEquipmentItem}
          className="w-full border-2 border-dashed border-gray-300 px-4 py-3 rounded text-gray-600 hover:border-blue-500 hover:text-blue-600"
        >
          + Add Another Equipment Item
        </button>

        <div className="flex gap-4">
          <button
            type="submit"
            className="flex-1 bg-blue-600 text-white px-6 py-3 rounded hover:bg-blue-700 font-semibold"
          >
            Create Work Order
          </button>
        </div>
      </form>
    </div>
  );
};

// Individual equipment item form
const EquipmentItemForm = ({ item, index, equipmentList, bootsList, skiModels, onUpdate, onRemove, canRemove }) => {
  const [brands, setBrands] = useState([]);
  const [models, setModels] = useState([]);

  useEffect(() => {
    if (skiModels.length > 0) {
      const uniqueBrands = [...new Set(skiModels.map(ski => ski.brand))];
      setBrands(uniqueBrands);
    }
  }, [skiModels]);

  useEffect(() => {
    if (item.newEquipment?.brand && skiModels.length > 0) {
      const brandModels = skiModels
        .filter(ski => ski.brand === item.newEquipment.brand)
        .map(ski => ({ model: ski.model, length: ski.length }));
      setModels(brandModels);
    }
  }, [item.newEquipment?.brand, skiModels]);

  return (
    <div className="border border-gray-300 rounded-lg p-6 space-y-4 relative">
      {canRemove && (
        <button
          type="button"
          onClick={onRemove}
          className="absolute top-4 right-4 text-red-600 hover:text-red-800 font-bold"
        >
          ✕ Remove
        </button>
      )}

      <h3 className="text-lg font-semibold">Equipment Item #{index + 1}</h3>

      {/* Service Type */}
      <div>
        <label className="block mb-1 font-semibold">Service Type *</label>
        <select
          value={item.serviceType}
          onChange={(e) => onUpdate({ serviceType: e.target.value })}
          className="w-full border border-gray-300 px-3 py-2 rounded"
          required
        >
          <option value="">Select Service</option>
          <option value="TUNE">Tune</option>
          <option value="MOUNT">Mount</option>
          <option value="REPAIR">Repair</option>
        </select>
      </div>

      {/* Equipment Selection */}
      <div className="space-y-3">
        <label className="block font-semibold">Equipment *</label>
        
        {equipmentList.length > 0 && (
          <div className="space-y-2">
            <label className="flex items-center gap-2">
              <input
                type="radio"
                checked={item.useExisting}
                onChange={() => onUpdate({ useExisting: true, newEquipment: null })}
              />
              <span className="font-medium">Use Existing Equipment</span>
            </label>

            {item.useExisting && (
              <select
                value={item.equipmentId || ''}
                onChange={(e) => onUpdate({ equipmentId: parseInt(e.target.value) })}
                className="w-full border border-gray-300 px-3 py-2 rounded"
                required
              >
                <option value="">Select Equipment</option>
                {equipmentList.map(eq => (
                  <option key={eq.id} value={eq.id}>
                    {eq.brand} {eq.model} {eq.length && `(${eq.length}cm)`} - {eq.condition}
                  </option>
                ))}
              </select>
            )}
          </div>
        )}

        <label className="flex items-center gap-2">
          <input
            type="radio"
            checked={!item.useExisting}
            onChange={() => onUpdate({ 
              useExisting: false, 
              equipmentId: null,
              newEquipment: { brand: '', model: '', condition: '', abilityLevel: '', length: null }
            })}
          />
          <span className="font-medium">Create New Equipment</span>
        </label>

        {!item.useExisting && (
          <div className="pl-6 space-y-3 border-l-2 border-blue-300">
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block mb-1 text-sm">Brand *</label>
                <select
                  value={item.newEquipment?.brand || ''}
                  onChange={(e) => onUpdate({ 
                    newEquipment: { ...item.newEquipment, brand: e.target.value, model: '' }
                  })}
                  className="w-full border border-gray-300 px-3 py-2 rounded text-sm"
                  required
                >
                  <option value="">Select Brand</option>
                  {brands.map(brand => (
                    <option key={brand} value={brand}>{brand}</option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block mb-1 text-sm">Model *</label>
                <select
                  value={item.newEquipment?.model || ''}
                  onChange={(e) => {
                    const selectedModel = models.find(m => m.model === e.target.value);
                    onUpdate({ 
                      newEquipment: { 
                        ...item.newEquipment, 
                        model: e.target.value,
                        length: selectedModel?.length || null
                      }
                    });
                  }}
                  className="w-full border border-gray-300 px-3 py-2 rounded text-sm"
                  required
                  disabled={!item.newEquipment?.brand}
                >
                  <option value="">Select Model</option>
                  {models.map(m => (
                    <option key={m.model} value={m.model}>
                      {m.model} {m.length && `(${m.length}cm)`}
                    </option>
                  ))}
                </select>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block mb-1 text-sm">Condition *</label>
                <select
                  value={item.newEquipment?.condition || ''}
                  onChange={(e) => onUpdate({ 
                    newEquipment: { ...item.newEquipment, condition: e.target.value }
                  })}
                  className="w-full border border-gray-300 px-3 py-2 rounded text-sm"
                  required
                >
                  <option value="">Select Condition</option>
                  <option value="NEW">New</option>
                  <option value="USED">Used</option>
                </select>
              </div>

              <div>
                <label className="block mb-1 text-sm">Ability Level *</label>
                <select
                  value={item.newEquipment?.abilityLevel || ''}
                  onChange={(e) => onUpdate({ 
                    newEquipment: { ...item.newEquipment, abilityLevel: e.target.value }
                  })}
                  className="w-full border border-gray-300 px-3 py-2 rounded text-sm"
                  required
                >
                  <option value="">Select Level</option>
                  <option value="BEGINNER">Beginner</option>
                  <option value="INTERMEDIATE">Intermediate</option>
                  <option value="ADVANCED">Advanced</option>
                </select>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Mount-specific fields */}
      {item.serviceType === 'MOUNT' && (
        <MountFields
          item={item}
          bootsList={bootsList}
          onUpdate={onUpdate}
        />
      )}
    </div>
  );
};

// Mount-specific fields component
const MountFields = ({ item, bootsList, onUpdate }) => {
  return (
    <div className="bg-blue-50 border border-blue-200 p-4 rounded space-y-4">
      <h4 className="font-semibold text-blue-900">Mount Service Requirements</h4>

      {/* Binding Info */}
      <div className="grid grid-cols-2 gap-3">
        <div>
          <label className="block mb-1 text-sm font-semibold">Binding Brand *</label>
          <input
            type="text"
            value={item.bindingBrand}
            onChange={(e) => onUpdate({ bindingBrand: e.target.value })}
            className="w-full border border-gray-300 px-3 py-2 rounded"
            placeholder="e.g., Marker"
            required
          />
        </div>

        <div>
          <label className="block mb-1 text-sm">Binding Model</label>
          <input
            type="text"
            value={item.bindingModel}
            onChange={(e) => onUpdate({ bindingModel: e.target.value })}
            className="w-full border border-gray-300 px-3 py-2 rounded"
            placeholder="e.g., Griffon 13"
          />
        </div>
      </div>

      {/* Boot Selection */}
      <div className="space-y-3">
        <label className="block font-semibold">Boot Information *</label>
        
        {bootsList.length > 0 && (
          <div>
            <label className="flex items-center gap-2 mb-2">
              <input
                type="radio"
                checked={!!item.bootId}
                onChange={() => onUpdate({ bootId: bootsList[0]?.id || null })}
              />
              <span>Use Existing Boot</span>
            </label>

            {!!item.bootId && (
              <select
                value={item.bootId || ''}
                onChange={(e) => onUpdate({ bootId: parseInt(e.target.value) })}
                className="w-full border border-gray-300 px-3 py-2 rounded"
                required
              >
                {bootsList.map(boot => (
                  <option key={boot.id} value={boot.id}>
                    {boot.brand} {boot.model} - BSL: {boot.bsl}mm
                  </option>
                ))}
              </select>
            )}
          </div>
        )}

        <label className="flex items-center gap-2">
          <input
            type="radio"
            checked={!item.bootId}
            onChange={() => onUpdate({ bootId: null })}
          />
          <span>Add New Boot</span>
        </label>

        {!item.bootId && (
          <div className="pl-6 space-y-3 border-l-2 border-blue-400">
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block mb-1 text-sm">Boot Brand *</label>
                <input
                  type="text"
                  value={item.bootBrand}
                  onChange={(e) => onUpdate({ bootBrand: e.target.value })}
                  className="w-full border border-gray-300 px-3 py-2 rounded text-sm"
                  required
                />
              </div>

              <div>
                <label className="block mb-1 text-sm">Boot Model *</label>
                <input
                  type="text"
                  value={item.bootModel}
                  onChange={(e) => onUpdate({ bootModel: e.target.value })}
                  className="w-full border border-gray-300 px-3 py-2 rounded text-sm"
                  required
                />
              </div>
            </div>

            <div className="grid grid-cols-3 gap-3">
              <div>
                <label className="block mb-1 text-sm">BSL (mm) *</label>
                <input
                  type="number"
                  value={item.bsl}
                  onChange={(e) => onUpdate({ bsl: e.target.value })}
                  className="w-full border border-gray-300 px-3 py-2 rounded text-sm"
                  min="250"
                  max="400"
                  required
                />
              </div>

              <div>
                <label className="block mb-1 text-sm">Weight (lbs) *</label>
                <input
                  type="number"
                  value={item.weight}
                  onChange={(e) => onUpdate({ weight: e.target.value })}
                  className="w-full border border-gray-300 px-3 py-2 rounded text-sm"
                  min="50"
                  max="400"
                  required
                />
              </div>

              <div>
                <label className="block mb-1 text-sm">Age *</label>
                <input
                  type="number"
                  value={item.age}
                  onChange={(e) => onUpdate({ age: e.target.value })}
                  className="w-full border border-gray-300 px-3 py-2 rounded text-sm"
                  min="5"
                  max="120"
                  required
                />
              </div>
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block mb-1 text-sm">Height *</label>
                <div className="flex gap-2">
                  <select
                    value={item.heightFeet}
                    onChange={(e) => onUpdate({ heightFeet: e.target.value })}
                    className="flex-1 border border-gray-300 px-3 py-2 rounded text-sm"
                    required
                  >
                    <option value="">Feet</option>
                    {[4, 5, 6, 7, 8].map(f => (
                      <option key={f} value={f}>{f}'</option>
                    ))}
                  </select>
                  <select
                    value={item.heightInches}
                    onChange={(e) => onUpdate({ heightInches: e.target.value })}
                    className="flex-1 border border-gray-300 px-3 py-2 rounded text-sm"
                    required
                  >
                    <option value="">Inches</option>
                    {[0,1,2,3,4,5,6,7,8,9,10,11].map(i => (
                      <option key={i} value={i}>{i}"</option>
                    ))}
                  </select>
                </div>
              </div>

              <div>
                <label className="block mb-1 text-sm">Ability Level *</label>
                <select
                  value={item.abilityLevel}
                  onChange={(e) => onUpdate({ abilityLevel: e.target.value })}
                  className="w-full border border-gray-300 px-3 py-2 rounded text-sm"
                  required
                >
                  <option value="">Select Level</option>
                  <option value="BEGINNER">Beginner</option>
                  <option value="INTERMEDIATE">Intermediate</option>
                  <option value="ADVANCED">Advanced</option>
                </select>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default EquipmentSelection;
