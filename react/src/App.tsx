import { BrowserRouter as Router, Routes, Route, Link } from "react-router-dom";
import Home from "./pages/Home";
import About from "./pages/About";
import Login from "./pages/Login";
import ApiDemo from "./pages/ApiDemo";
import Register from "./pages/Register";
import User from "./pages/User";

const navLinkStyle: React.CSSProperties = {
  textDecoration: "none",
  color: "#007bff",
  padding: "8px 12px",
  borderRadius: "4px",
  transition: "background-color 0.2s",
};

const navLinkHoverStyle = { backgroundColor: "#f0f7ff" };

export default function App() {
  return (
    <Router>
      <div style={{ padding: 20, fontFamily: "Arial, sans-serif", maxWidth: 800, margin: "0 auto" }}>
        <nav style={{ marginBottom: "20px", borderBottom: "1px solid #eee", paddingBottom: "10px" }}>
          <ul style={{ listStyle: "none", padding: 0, display: "flex", gap: "20px" }}>
            <li>
              <Link to="/" style={navLinkStyle} onMouseOver={(e) => (e.currentTarget.style.backgroundColor = navLinkHoverStyle.backgroundColor)} onMouseOut={(e) => (e.currentTarget.style.backgroundColor = "")}>
                Home
              </Link>
            </li>
            <li>
              <Link to="/about" style={navLinkStyle} onMouseOver={(e) => (e.currentTarget.style.backgroundColor = navLinkHoverStyle.backgroundColor)} onMouseOut={(e) => (e.currentTarget.style.backgroundColor = "")}>
                About
              </Link>
            </li>
            <li>
              <Link to="/demo" style={navLinkStyle} onMouseOver={(e) => (e.currentTarget.style.backgroundColor = navLinkHoverStyle.backgroundColor)} onMouseOut={(e) => (e.currentTarget.style.backgroundColor = "")}>
                API Demo
              </Link>
            </li>
            <li>
              <Link to="/login" style={navLinkStyle} onMouseOver={(e) => (e.currentTarget.style.backgroundColor = navLinkHoverStyle.backgroundColor)} onMouseOut={(e) => (e.currentTarget.style.backgroundColor = "")}>
                Login
              </Link>
            </li>
            
            <li>
              <Link
                to="/register"
                style={navLinkStyle}
                onMouseOver={(e) => (e.currentTarget.style.backgroundColor = navLinkHoverStyle.backgroundColor)}
                onMouseOut={(e) => (e.currentTarget.style.backgroundColor = "")}
              >
                Register
              </Link>
          </li>
          <li>
              <Link
                to="/user"
                style={navLinkStyle}
                onMouseOver={(e) => (e.currentTarget.style.backgroundColor = navLinkHoverStyle.backgroundColor)}
                onMouseOut={(e) => (e.currentTarget.style.backgroundColor = "")}
              >
                User
              </Link>
            </li>
          </ul>
        </nav>

        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/about" element={<About />} />
          <Route path="/demo" element={<ApiDemo />} /> 
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/user" element={<User />} />
        </Routes>
      </div>
    </Router>
  );
}
