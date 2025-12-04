import { useEffect, useState } from "react";
import { BrowserRouter as Router, Routes, Route, Link } from "react-router-dom";
import axios from "axios";
import Home from "./pages/Home";
import About from "./pages/About";
import Login from "./pages/Login";

// Axios defaults for XSRF and cookies
axios.defaults.withCredentials = true;
axios.defaults.xsrfCookieName = "XSRF-TOKEN";
axios.defaults.xsrfHeaderName = "X-XSRF-TOKEN";

const API_BASE = "/api";

function ApiDemo() {
  const [input, setInput] = useState("");
  const [responseText, setResponseText] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  // Fetch CSRF token once on mount
  useEffect(() => {
    axios
      .get(`${API_BASE}/csrf`)
      .catch((err) => {
        console.error("Failed to fetch CSRF token", err);
      });
  }, []);

  const handlePost = async () => {
    if (!input.trim()) {
      setResponseText("Enter a message before sending");
      return;
    }

    setLoading(true);
    setResponseText(null);
    try {
      const res = await axios.post(API_BASE, { message: input });
      setResponseText(JSON.stringify(res.data, null, 2));
      setInput("");
    } catch (err: any) {
      console.error("POST error", err);
      const serverMessage = err?.response?.data?.message || err.message || "Unknown error";
      setResponseText(`Error: ${serverMessage}`);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <h1>API Demo</h1>
      <div style={{ marginBottom: '20px' }}>
        <input
          type="text"
          value={input}
          onChange={(e) => setInput(e.target.value)}
          placeholder="Type a message..."
          style={{ padding: '8px', marginRight: '10px', width: '300px' }}
        />
        <button 
          onClick={handlePost} 
          disabled={loading}
          style={{
            padding: '8px 16px',
            backgroundColor: loading ? '#ccc' : '#007bff',
            color: 'white',
            border: 'none',
            borderRadius: '4px',
            cursor: loading ? 'not-allowed' : 'pointer'
          }}
        >
          {loading ? 'Sending...' : 'Send Message'}
        </button>
      </div>
      {responseText && (
        <div style={{
          marginTop: '20px',
          padding: '15px',
          backgroundColor: '#f8f9fa',
          borderRadius: '4px',
          whiteSpace: 'pre-wrap',
          fontFamily: 'monospace',
          maxHeight: '300px',
          overflow: 'auto'
        }}>
          {responseText}
        </div>
      )}
    </div>
  );
}

const navLinkStyle: React.CSSProperties = {
  textDecoration: 'none',
  color: '#007bff',
  padding: '8px 12px',
  borderRadius: '4px',
  transition: 'background-color 0.2s',
};

const navLinkHoverStyle = {
  backgroundColor: '#f0f7ff'
};

export default function App() {
  return (
    <Router>
      <div style={{ padding: 20, fontFamily: "Arial, sans-serif", maxWidth: 800, margin: '0 auto' }}>
        <nav style={{ marginBottom: '20px', borderBottom: '1px solid #eee', paddingBottom: '10px' }}>
          <ul style={{ listStyle: 'none', padding: 0, display: 'flex', gap: '20px' }}>
            <li><Link to="/" style={navLinkStyle} onMouseOver={e => (e.currentTarget.style.backgroundColor = navLinkHoverStyle.backgroundColor)}
                                                      onMouseOut={e => (e.currentTarget.style.backgroundColor = '')}>Home</Link></li>
            <li><Link to="/about" style={navLinkStyle} onMouseOver={e => (e.currentTarget.style.backgroundColor = navLinkHoverStyle.backgroundColor)}
                                                       onMouseOut={e => (e.currentTarget.style.backgroundColor = '')}>About</Link></li>
            <li><Link to="/demo" style={navLinkStyle} onMouseOver={e => (e.currentTarget.style.backgroundColor = navLinkHoverStyle.backgroundColor)}
                                                      onMouseOut={e => (e.currentTarget.style.backgroundColor = '')}>API Demo</Link></li>
            <li><Link to="/login" style={navLinkStyle} onMouseOver={e => (e.currentTarget.style.backgroundColor = navLinkHoverStyle.backgroundColor)}
                                                      onMouseOut={e => (e.currentTarget.style.backgroundColor = '')}>Login</Link></li>
          </ul>
        </nav>

        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/about" element={<About />} />
          <Route path="/demo" element={<ApiDemo />} />
          <Route path="/login" element={<Login />} />
        </Routes>
      </div>
    </Router>
  );
}
