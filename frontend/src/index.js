import './index.css';
import React, { useEffect } from 'react';
import ReactDOM from 'react-dom';
import SearchEngine from './components/SearchEngine/SearchEngine';

function App() {
  useEffect(() => {
    const darkMode = localStorage.getItem('dark-mode') === 'true';
    document.body.classList.toggle('dark-mode', darkMode);
  }, []);

  const toggleDarkMode = () => {
    const body = document.body;
    const darkMode = body.classList.toggle('dark-mode');
    localStorage.setItem('dark-mode', darkMode);
  };

  return (
    <React.StrictMode>
      <div className="dark-mode-toggle">
        <SearchEngine />
        <button onClick={toggleDarkMode}>Toggle Dark Mode</button>
      </div>
    </React.StrictMode>
  );
}

ReactDOM.render(<App />, document.getElementById('root'));
