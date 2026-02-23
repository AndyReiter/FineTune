import { useState } from "react";

export default function ShopBrandingSettings() {
  const [shopName, setShopName] = useState("");
  const [logoFile, setLogoFile] = useState(null);
  const [logoPreview, setLogoPreview] = useState("");
  const [message, setMessage] = useState("");
  const [messageType, setMessageType] = useState("");

  const fetchBranding = async () => {
    try {
      const res = await fetch("http://localhost:8080/shops");
      const shops = await res.json();
      if (shops.length > 0) {
        setShopName(shops[0].name || "");
        setLogoPreview(shops[0].logoUrl ? `http://localhost:8080${shops[0].logoUrl}` : "");
      }
    } catch (e) {
      setMessage("Failed to load branding");
      setMessageType("error");
    }
  };

  const handleLogoChange = (e) => {
    const file = e.target.files[0];
    setLogoFile(file);
    if (file) {
      const reader = new FileReader();
      reader.onload = (ev) => setLogoPreview(ev.target.result);
      reader.readAsDataURL(file);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const formData = new FormData();
    formData.append("shopName", shopName);
    if (logoFile) formData.append("logo", logoFile);
    try {
      const res = await fetch("http://localhost:8080/shops/branding", {
        method: "POST",
        body: formData,
      });
      if (res.ok) {
        setMessage("Branding saved!");
        setMessageType("success");
        fetchBranding();
      } else {
        setMessage("Failed to save branding");
        setMessageType("error");
      }
    } catch (e) {
      setMessage("Error saving branding");
      setMessageType("error");
    }
  };

  // Load branding on mount
  React.useEffect(() => {
    fetchBranding();
  }, []);

  return (
    <div className="settings-card">
      <div className="card-header-section">
        <h2 className="card-title">
          <i className="fas fa-store"></i> Shop Branding
        </h2>
        <p className="card-description">Upload your shop logo to display on agreements and throughout the dashboard</p>
      </div>
      {message && (
        <div className={`alert alert-${messageType}`}>{message}</div>
      )}
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label className="form-label">
            <i className="fas fa-image"></i> Current Logo
          </label>
          <div style={{marginTop:12, padding:20, background:'#f8fafc', border:'2px dashed #cbd5e1', borderRadius:8, textAlign:'center'}}>
            {logoPreview ? (
              <img src={logoPreview} alt="Shop Logo" style={{maxWidth:200, maxHeight:100, margin:'0 auto'}} />
            ) : (
              <p style={{color:'#64748b', margin:0}}>
                <i className="fas fa-image" style={{fontSize:48, color:'#cbd5e1', display:'block', marginBottom:12}}></i>
                No logo uploaded
              </p>
            )}
          </div>
        </div>
        <div className="form-group">
          <label className="form-label">
            <i className="fas fa-upload"></i> Upload Shop Logo
          </label>
          <input type="file" accept="image/*" onChange={handleLogoChange} className="form-input" />
        </div>
        <div className="form-group">
          <label className="form-label">
            <i className="fas fa-tag"></i> Shop Name
          </label>
          <input type="text" value={shopName} onChange={e => setShopName(e.target.value)} className="form-input" placeholder="Your Shop Name" />
        </div>
        <div className="form-actions">
          <button type="submit" className="btn-primary">
            <i className="fas fa-save"></i> Save Branding
          </button>
        </div>
      </form>
    </div>
  );
}
