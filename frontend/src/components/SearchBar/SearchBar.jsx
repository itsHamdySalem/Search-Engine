import React, { useState } from "react";

const SearchBar = ({ onSearch }) => {
    const [query, setQuery] = useState('');

    const handleQueryChange = (event) => {
        setQuery(event.target.value);
    };

    const handleQuerySubmit = (event) => {
        event.preventDefault();
        onSearch(query);
    };

    return (
        <form className="search-form" onSubmit={handleQuerySubmit}>
            <input
                type="text"
                className="search-input"
                value={query}
                onChange={handleQueryChange}
                placeholder="Search..."
            />
            <button type="submit" className="search-button">
                Search
            </button>
        </form>
    );
};

export default SearchBar;