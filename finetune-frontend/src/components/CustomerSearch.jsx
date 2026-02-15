import React, { useState, useEffect } from 'react';
import { searchCustomers, lookupCustomer, createCustomer } from '../services/api';

const CustomerSearch = ({ onCustomerSelected }) => {
  const [searchMode, setSearchMode] = useState('search'); // 'search' or 'create'
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [searching, setSearching] = useState(false);
  const [error, setError] = useState(null);
  
  // New customer form
  const [newCustomer, setNewCustomer] = useState({
    firstName: '',
    lastName: '',
    email: '',
    phone: ''
  });

  // Real-time search with debouncing
  useEffect(() => {
    const delaySearch = setTimeout(() => {
      if (searchQuery.length >= 2 && searchMode === 'search') {
        performSearch();
      } else {
        setSearchResults([]);
      }
    }, 300);

    return () => clearTimeout(delaySearch);
  }, [searchQuery]);

  const performSearch = async () => {
    setSearching(true);
    setError(null);
    
    try {
      // Determine search type based on query format
      const query = {};
      
      if (searchQuery.includes('@')) {
        query.email = searchQuery;
      } else if (/^\d+$/.test(searchQuery)) {
        query.phone = searchQuery;
      } else {
        query.name = searchQuery;
      }
      
      const results = await searchCustomers(query);
      setSearchResults(results || []);
    } catch (err) {
      setError('Failed to search customers');
      setSearchResults([]);
    } finally {
      setSearching(false);
    }
  };

  const handleSelectCustomer = (customer) => {
    onCustomerSelected(customer);
  };

  const handleCreateCustomer = async (e) => {
    e.preventDefault();
    setError(null);
    
    // Validation
    if (!newCustomer.firstName || !newCustomer.lastName || !newCustomer.email || !newCustomer.phone) {
      setError('All fields are required');
      return;
    }
    
    if (newCustomer.phone.length !== 10) {
      setError('Phone number must be 10 digits');
      return;
    }
    
    if (!newCustomer.email.includes('@')) {
      setError('Invalid email address');
      return;
    }
    
    try {
      // Check for duplicate first
      const existing = await lookupCustomer(newCustomer.email, newCustomer.phone);
      
      if (existing) {
        if (window.confirm('A customer with this email/phone already exists. Use existing customer?')) {
          onCustomerSelected(existing);
          return;
        } else {
          return;
        }
      }
      
      // Create new customer
      const created = await createCustomer(newCustomer);
      onCustomerSelected(created);
    } catch (err) {
      setError('Failed to create customer: ' + err.message);
    }
  };

  const handleNewCustomerChange = (e) => {
    const { name, value } = e.target;
    
    // Format phone number
    if (name === 'phone') {
      const numericValue = value.replace(/\D/g, '').slice(0, 10);
      setNewCustomer({ ...newCustomer, [name]: numericValue });
    } else {
      setNewCustomer({ ...newCustomer, [name]: value });
    }
  };

  return (
    <div className="space-y-6">
      <h2 className="text-2xl font-bold">Step 1: Select or Create Customer</h2>
      
      {/* Mode Toggle */}
      <div className="flex gap-4 border-b pb-4">
        <button
          onClick={() => setSearchMode('search')}
          className={`px-4 py-2 rounded ${
            searchMode === 'search' 
              ? 'bg-blue-600 text-white' 
              : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
          }`}
        >
          Search Existing Customer
        </button>
        <button
          onClick={() => setSearchMode('create')}
          className={`px-4 py-2 rounded ${
            searchMode === 'create' 
              ? 'bg-blue-600 text-white' 
              : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
          }`}
        >
          Create New Customer
        </button>
      </div>

      {error && (
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
          {error}
        </div>
      )}

      {/* Search Mode */}
      {searchMode === 'search' && (
        <div className="space-y-4">
          <div>
            <label className="block mb-2 font-semibold">
              Search by Name, Email, or Phone
            </label>
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Enter name, email, or phone number..."
              className="w-full border border-gray-300 px-4 py-2 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
            <p className="text-sm text-gray-600 mt-1">
              Type at least 2 characters to search
            </p>
          </div>

          {/* Search Results */}
          {searching && (
            <div className="text-center py-4 text-gray-600">Searching...</div>
          )}

          {!searching && searchQuery.length >= 2 && searchResults.length === 0 && (
            <div className="text-center py-4 text-gray-600">
              No customers found. Try creating a new customer.
            </div>
          )}

          {searchResults.length > 0 && (
            <div className="space-y-2">
              <h3 className="font-semibold">Search Results:</h3>
              <div className="border border-gray-300 rounded divide-y max-h-96 overflow-y-auto">
                {searchResults.map((customer) => (
                  <div
                    key={customer.id}
                    onClick={() => handleSelectCustomer(customer)}
                    className="p-4 hover:bg-blue-50 cursor-pointer transition"
                  >
                    <div className="font-semibold">
                      {customer.firstName} {customer.lastName}
                    </div>
                    <div className="text-sm text-gray-600">
                      {customer.email} | {customer.phone}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      )}

      {/* Create Mode */}
      {searchMode === 'create' && (
        <form onSubmit={handleCreateCustomer} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block mb-1 font-semibold">First Name *</label>
              <input
                type="text"
                name="firstName"
                value={newCustomer.firstName}
                onChange={handleNewCustomerChange}
                className="w-full border border-gray-300 px-3 py-2 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                required
              />
            </div>

            <div>
              <label className="block mb-1 font-semibold">Last Name *</label>
              <input
                type="text"
                name="lastName"
                value={newCustomer.lastName}
                onChange={handleNewCustomerChange}
                className="w-full border border-gray-300 px-3 py-2 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                required
              />
            </div>
          </div>

          <div>
            <label className="block mb-1 font-semibold">Email *</label>
            <input
              type="email"
              name="email"
              value={newCustomer.email}
              onChange={handleNewCustomerChange}
              className="w-full border border-gray-300 px-3 py-2 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
              required
            />
          </div>

          <div>
            <label className="block mb-1 font-semibold">Phone *</label>
            <input
              type="tel"
              name="phone"
              value={newCustomer.phone}
              onChange={handleNewCustomerChange}
              placeholder="10-digit phone number"
              maxLength={10}
              className="w-full border border-gray-300 px-3 py-2 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
              required
            />
          </div>

          <button
            type="submit"
            className="w-full bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700 font-semibold"
          >
            Create Customer & Continue
          </button>
        </form>
      )}
    </div>
  );
};

export default CustomerSearch;
