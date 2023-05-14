import React from "react";

const SearchResult = ({ title, url, snippet }) => {
    <div className="search-result">
        <h3 className="result-title">{title}</h3>
        <a className="result-url" href={url}>{url}</a>
        <p className="result-snippet">{snippet}</p>
    </div>
};

export default SearchResult;