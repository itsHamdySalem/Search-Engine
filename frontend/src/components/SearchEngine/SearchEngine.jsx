import React, { useState } from "react";
import SearchBar from "../SearchBar/SearchBar";
import SearchResults from "../SearchResults/SearchResults";

const SearchEngine = () => {
    const [results, setResults] = useState([]);
    const [searchTime, setSearchTime] = useState('');

    const performSearch = (query) => {
        currentTime = new Date().toLocaleTimeString();
        // TODO: Perform search engine request
        
        const mockResults = [
            {
                title: 'Example Website 1',
                url: 'https://www.example.com/1',
                snippet: 'This is an example snippet containing the query words.',
            },
            {
                title: 'Example Website 2',
                url: 'https://www.example.com/2',
                snippet: 'Another example snippet with the query words highlighted.',
            },
        ];

        setResults(mockResults);
        setSearchTime(new Date().toLocaleTimeString() - currentTime);
    };

    return (
        <div>
            <SearchBar onSearch={performSearch} />
            <p>Search time: {searchTime}</p>
            <SearchResults results={results} />
        </div>
    );
};

export default SearchEngine;