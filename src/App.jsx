import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { Toaster } from "react-hot-toast";
const queryClient = new QueryClient();
// Placeholder pages — replace with your actual page components
const Dashboard = () => <div style={{padding:"2rem"}}><h1>Dashboard</h1></div>;
const Login = () => <div style={{padding:"2rem"}}><h1>Login</h1></div>;
const Transactions = () => <div style={{padding:"2rem"}}><h1>Transactions</h1></div>;
const Reports = () => <div style={{padding:"2rem"}}><h1>Reports</h1></div>;
const NotFound = () => <div style={{padding:"2rem"}}><h1>404 - Page Not Found</h1></div>;

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <Router>
        <Toaster position="top-right" />
        <Routes>
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route path="/login" element={<Login />} />
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/transactions" element={<Transactions />} />
          <Route path="/reports" element={<Reports />} />
          <Route path="*" element={<NotFound />} />
        </Routes>
      </Router>
    </QueryClientProvider>
  );
}

export default App;
