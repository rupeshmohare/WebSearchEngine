import { useState } from 'react'
import reactLogo from './assets/react.svg'
import viteLogo from '/vite.svg'
import './App.css'

function App() {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(false);

  const handleSearch = async (e) => {
    e.preventDefault();
    if (!query) return;
    setLoading(true);
    try {
      const response = await fetch(`http://localhost:8080/api/search?query=${encodeURIComponent(query)}`);
      const data = await response.json();
      setResults(data);
    } catch (error) {
      console.error("Search error:", error);
    }
    setLoading(false);
  };

  return (
    <div className="App">
      <h1>Web Search</h1>
      <form onSubmit={handleSearch}>
        <input
          type="text"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder="Search something..."
        />
        <button type="submit">{loading ? "Searching..." : "Search"}</button>
      </form>
      <div className="results">
        {results.map((item, index) => (
          <div key={index} className="result">
            <a href={item.url} target="_blank" rel="noreferrer">
              <h3>{item.title}</h3>
            </a>
            <p>{item.description}</p>
          </div>
        ))}
      </div>
    </div>
  );
}

export default App
