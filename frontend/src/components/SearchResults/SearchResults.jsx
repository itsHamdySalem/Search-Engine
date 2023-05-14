import React from "react";
import SearchResult from "../SearchResult/SearchResult";

const SearchResults = ({ results }) => {
    return (
        <div className="search-results">
            {results.map((result, index) => (
                <SearchResult
                    key={index}
                    title={result.title}
                    url={result.url}
                    snippet={result.snippet}
                />
            ))}
        </div>
    );
};

export default SearchResults;