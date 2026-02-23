import React, { useState, useRef, useEffect } from 'react';

/**
 * CustomerAgreementStep - Agreement signing component for public work order creation
 * 
 * Layout:
 * 1. Shop Logo
 * 2. Binding Mounting Agreement Title
 * 3. Scrollable Agreement Text Box
 * 4. Customer Legal Acknowledgements
 * 5. Signature Pad
 * 6. Typed Name Confirmation
 * 7. Agreement Checkbox
 * 8. Continue Button
 * 
 * @param {Object} shop - Shop data with logo and name
 * @param {Object} agreementTemplate - Agreement template with title and text
 * @param {Object} customer - Customer data with firstName and lastName for name validation
 * @param {Function} onSign - Callback when customer signs (receives signature data and name)
 * @param {Function} onBack - Callback to go back to previous step
 * 
 * @example
 * <CustomerAgreementStep
 *   shop={{ name: "Alpine Ski Shop", logoUrl: "https://..." }}
 *   agreementTemplate={{ title: "Binding Mounting Agreement", agreementText: "..." }}
 *   customer={{ firstName: "John", lastName: "Doe" }}
 *   onSign={(data) => {
 *     // data.signatureName: "John Doe"
 *     // data.signatureImageBase64: "data:image/png;base64,..."
 *     console.log('Signature captured:', data);
 *   }}
 *   onBack={() => console.log('Go back')}
 * />
 */
const CustomerAgreementStep = ({ shop, agreementTemplate, customer, onSign, onBack }) => {
  const [typedName, setTypedName] = useState('');
  const [hasScrolledToBottom, setHasScrolledToBottom] = useState(false);
  const [isScrollable, setIsScrollable] = useState(false);
  const [agreementChecked, setAgreementChecked] = useState(false);
  const [isDrawing, setIsDrawing] = useState(false);
  const [hasSignature, setHasSignature] = useState(false);
  const [nameValidationError, setNameValidationError] = useState('');
  
  const agreementContainerRef = useRef(null);
  const canvasRef = useRef(null);
  const contextRef = useRef(null);

  // Get expected full name from customer data
  const expectedFullName = customer 
    ? `${customer.firstName} ${customer.lastName}`.trim() 
    : '';

  // Initialize signature canvas
  // Lightweight canvas-based signature pad implementation
  // Requirements:
  // - Capture signature as base64 PNG
  // - Responsive on mobile (touch support)
  // - Smooth drawing with high DPI support
  // - Allow clearing signature
  // - Disable submission if empty
  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    // Set canvas size with high DPI support for retina displays
    const rect = canvas.getBoundingClientRect();
    canvas.width = rect.width * 2;
    canvas.height = rect.height * 2;
    
    const context = canvas.getContext('2d');
    context.scale(2, 2);
    context.lineCap = 'round';
    context.strokeStyle = '#000';
    context.lineWidth = 2;
    contextRef.current = context;
  }, []);

  // Check if content is scrollable
  useEffect(() => {
    const container = agreementContainerRef.current;
    if (!container) return;

    const checkScrollable = () => {
      const scrollable = container.scrollHeight > container.clientHeight;
      setIsScrollable(scrollable);
      
      if (!scrollable) {
        setHasScrolledToBottom(true);
      }
    };

    checkScrollable();
    window.addEventListener('resize', checkScrollable);
    return () => window.removeEventListener('resize', checkScrollable);
  }, [agreementTemplate]);

  // Handle scroll to detect bottom
  const handleScroll = () => {
    const container = agreementContainerRef.current;
    if (!container) return;

    const scrolledToBottom = 
      container.scrollHeight - container.scrollTop <= container.clientHeight + 10;

    if (scrolledToBottom && !hasScrolledToBottom) {
      setHasScrolledToBottom(true);
    }
  };

  // Signature pad drawing functions
  const startDrawing = (e) => {
    if (!hasScrolledToBottom) return;
    
    const { offsetX, offsetY } = getCoordinates(e);
    contextRef.current.beginPath();
    contextRef.current.moveTo(offsetX, offsetY);
    setIsDrawing(true);
    setHasSignature(true);
  };

  const draw = (e) => {
    if (!isDrawing) return;
    
    const { offsetX, offsetY } = getCoordinates(e);
    contextRef.current.lineTo(offsetX, offsetY);
    contextRef.current.stroke();
  };

  const stopDrawing = () => {
    contextRef.current.closePath();
    setIsDrawing(false);
  };

  const getCoordinates = (e) => {
    const canvas = canvasRef.current;
    const rect = canvas.getBoundingClientRect();
    
    // Handle both mouse and touch events
    const clientX = e.clientX || (e.touches && e.touches[0]?.clientX);
    const clientY = e.clientY || (e.touches && e.touches[0]?.clientY);
    
    return {
      offsetX: clientX - rect.left,
      offsetY: clientY - rect.top
    };
  };

  const clearSignature = () => {
    const canvas = canvasRef.current;
    const context = contextRef.current;
    context.clearRect(0, 0, canvas.width, canvas.height);
    setHasSignature(false);
  };

  // Validate typed name matches customer name (case insensitive)
  const validateTypedName = (name) => {
    setTypedName(name);
    
    if (!name.trim()) {
      setNameValidationError('');
      return;
    }
    
    if (!expectedFullName) {
      setNameValidationError('');
      return;
    }
    
    const typedNameLower = name.trim().toLowerCase();
    const expectedNameLower = expectedFullName.toLowerCase();
    
    if (typedNameLower !== expectedNameLower) {
      setNameValidationError(`Name must match: ${expectedFullName}`);
    } else {
      setNameValidationError('');
    }
  };

  const isNameValid = () => {
    if (!expectedFullName) return typedName.trim().length > 0;
    return typedName.trim().toLowerCase() === expectedFullName.toLowerCase();
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    
    if (canContinue) {
      const canvas = canvasRef.current;
      const signatureImageBase64 = canvas.toDataURL('image/png');
      
      onSign({
        signatureName: typedName.trim(),
        signatureImageBase64: signatureImageBase64
      });
    }
  };

  const canContinue = 
    hasScrolledToBottom && 
    hasSignature && 
    isNameValid() && 
    agreementChecked;

  return (
    <div className="max-w-4xl mx-auto p-6">
      {/* ===== Shop Logo ===== */}
      <div className="text-center mb-8">
        {shop?.logoUrl && (
          <img 
            src={shop.logoUrl} 
            alt={shop.name || 'Shop Logo'} 
            className="mx-auto max-h-24 object-contain mb-4"
          />
        )}
        {shop?.name && (
          <p className="text-lg text-gray-600 font-medium">{shop.name}</p>
        )}
      </div>

      {/* ===== Binding Mounting Agreement Title ===== */}
      <div className="bg-white rounded-t-lg border-t border-x border-gray-300 px-6 py-4">
        <h2 className="text-2xl font-bold text-gray-900 text-center">
          {agreementTemplate?.title || 'Binding Mounting Agreement'}
        </h2>
      </div>

      {/* ===== Scrollable Agreement Text Box ===== */}
      <div className="bg-white border-x border-gray-300">
        {isScrollable && !hasScrolledToBottom && (
          <div className="px-6 py-3 bg-amber-50 border-b border-amber-200">
            <p className="text-sm text-amber-800 flex items-center">
              <svg className="w-4 h-4 mr-2" fill="currentColor" viewBox="0 0 20 20">
                <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clipRule="evenodd" />
              </svg>
              Please scroll to the bottom to read the complete agreement
            </p>
          </div>
        )}
        
        <div 
          ref={agreementContainerRef}
          onScroll={handleScroll}
          className="px-6 py-6 overflow-y-auto bg-gray-50"
          style={{ height: '400px' }}
        >
          <div 
            className="prose prose-sm max-w-none text-gray-800 leading-relaxed whitespace-pre-wrap"
            dangerouslySetInnerHTML={{ 
              __html: agreementTemplate?.agreementText || 'Agreement text not available.' 
            }}
          />
        </div>
      </div>

      {/* ===== Customer Legal Acknowledgements, Signature Pad, Name, Checkbox ===== */}
      <form onSubmit={handleSubmit} className="bg-white rounded-b-lg border border-gray-300 px-6 py-6">
        
        {/* Legal Acknowledgement */}
        <div className="mb-6 p-4 bg-blue-50 border border-blue-200 rounded-lg">
          <p className="text-sm text-gray-700 leading-relaxed">
            <strong className="text-gray-900">Electronic Signature Agreement:</strong><br />
            By signing below, I acknowledge that I have read, understood, and agree to the terms 
            and conditions outlined in this agreement. I understand that my electronic signature 
            is legally binding and has the same effect as a handwritten signature in accordance 
            with the ESIGN Act and UETA.
          </p>
        </div>

        {/* Signature Pad */}
        <div className="mb-6">
          <label className="block text-sm font-semibold text-gray-700 mb-2">
            Signature <span className="text-red-500">*</span>
          </label>
          <div className="border-2 border-gray-300 rounded-lg bg-white relative">
            <canvas
              ref={canvasRef}
              onMouseDown={startDrawing}
              onMouseMove={draw}
              onMouseUp={stopDrawing}
              onMouseLeave={stopDrawing}
              onTouchStart={startDrawing}
              onTouchMove={draw}
              onTouchEnd={stopDrawing}
              className={`w-full touch-none ${
                hasScrolledToBottom ? 'cursor-crosshair' : 'cursor-not-allowed opacity-50'
              }`}
              style={{ height: '150px' }}
            />
            {!hasSignature && (
              <div className="absolute inset-0 flex items-center justify-center pointer-events-none">
                <p className="text-gray-400 text-sm">
                  {hasScrolledToBottom ? 'Sign here with your mouse or finger' : 'Scroll to bottom first'}
                </p>
              </div>
            )}
          </div>
          <button
            type="button"
            onClick={clearSignature}
            disabled={!hasSignature}
            className="mt-2 text-sm text-blue-600 hover:text-blue-800 disabled:text-gray-400 disabled:cursor-not-allowed"
          >
            Clear Signature
          </button>
        </div>

        {/* Typed Name Confirmation */}
        <div className="mb-6">
          <label htmlFor="typedName" className="block text-sm font-semibold text-gray-700 mb-2">
            Full Legal Name <span className="text-red-500">*</span>
          </label>
          {expectedFullName && (
            <p className="text-xs text-gray-600 mb-2">
              Please enter: {expectedFullName}
            </p>
          )}
          <input
            id="typedName"
            type="text"
            value={typedName}
            onChange={(e) => validateTypedName(e.target.value)}
            disabled={!hasScrolledToBottom}
            placeholder="Enter your full legal name"
            className={`w-full px-4 py-3 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 ${
              hasScrolledToBottom 
                ? nameValidationError 
                  ? 'bg-white border-red-500' 
                  : 'bg-white border-gray-300'
                : 'bg-gray-100 border-gray-200 cursor-not-allowed'
            }`}
            required
          />
          {nameValidationError && (
            <p className="mt-1 text-sm text-red-600">
              {nameValidationError}
            </p>
          )}
        </div>

        {/* Agreement Checkbox */}
        <div className="mb-6">
          <label className="flex items-start">
            <input
              type="checkbox"
              checked={agreementChecked}
              onChange={(e) => setAgreementChecked(e.target.checked)}
              disabled={!hasScrolledToBottom}
              className="mt-1 h-5 w-5 text-blue-600 border-gray-300 rounded focus:ring-blue-500 disabled:cursor-not-allowed disabled:opacity-50"
              required
            />
            <span className="ml-3 text-sm text-gray-700">
              I have read and agree to the Binding Mounting Agreement and understand this is a legally binding contract. <span className="text-red-500">*</span>
            </span>
          </label>
        </div>

        {/* Status Messages */}
        {!hasScrolledToBottom && isScrollable && (
          <div className="mb-6 bg-amber-50 border border-amber-200 rounded-lg p-4">
            <p className="text-sm text-amber-800 flex items-center">
              <svg className="w-5 h-5 mr-2 flex-shrink-0" fill="currentColor" viewBox="0 0 20 20">
                <path fillRule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
              </svg>
              Please scroll to the bottom of the agreement before signing
            </p>
          </div>
        )}

        {hasScrolledToBottom && !canContinue && (
          <div className="mb-6 bg-blue-50 border border-blue-200 rounded-lg p-4">
            <p className="text-sm text-blue-800 font-semibold mb-2">
              Complete all required fields:
            </p>
            <ul className="text-sm text-blue-800 list-disc list-inside space-y-1">
              {!hasSignature && <li>Draw your signature in the signature pad</li>}
              {!isNameValid() && <li>Enter your full legal name exactly as shown</li>}
              {!agreementChecked && <li>Check the agreement acknowledgement box</li>}
            </ul>
          </div>
        )}

        {/* Continue Button */}
        <div className="flex gap-4 pt-4 border-t border-gray-200">
          <button
            type="button"
            onClick={onBack}
            className="flex-1 px-6 py-3 border-2 border-gray-300 rounded-lg hover:bg-gray-50 font-semibold text-gray-700 transition-colors"
          >
            ← Back
          </button>
          <button
            type="submit"
            disabled={!canContinue}
            className={`flex-1 px-6 py-3 rounded-lg font-semibold transition-all ${
              canContinue
                ? 'bg-blue-600 text-white hover:bg-blue-700 shadow-md hover:shadow-lg'
                : 'bg-gray-300 text-gray-500 cursor-not-allowed'
            }`}
          >
            Continue →
          </button>
        </div>
      </form>

      {/* Legal Footer */}
      <div className="mt-4 text-center">
        <p className="text-xs text-gray-500">
          This electronic signature process complies with the ESIGN Act and UETA
        </p>
      </div>
    </div>
  );
};

export default CustomerAgreementStep;
