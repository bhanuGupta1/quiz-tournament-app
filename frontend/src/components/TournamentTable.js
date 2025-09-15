import React, { useState } from 'react';
import './TournamentTable.css';

const TournamentTable = ({ 
  tournaments, 
  onEdit, 
  onDelete, 
  onViewQuestions,
  onViewDetails,
  onViewDetailedAnswers 
}) => {
  const [sortField, setSortField] = useState('name');
  const [sortDirection, setSortDirection] = useState('asc');

  const handleSort = (field) => {
    if (sortField === field) {
      setSortDirection(sortDirection === 'asc' ? 'desc' : 'asc');
    } else {
      setSortField(field);
      setSortDirection('asc');
    }
  };

  const sortedTournaments = [...tournaments].sort((a, b) => {
    let aValue = a[sortField];
    let bValue = b[sortField];

    // Handle nested creator field
    if (sortField === 'creator') {
      aValue = a.createdBy?.username || '';
      bValue = b.createdBy?.username || '';
    }

    // Handle date fields
    if (sortField === 'startDate' || sortField === 'endDate') {
      aValue = new Date(aValue);
      bValue = new Date(bValue);
    }

    if (aValue < bValue) {
      return sortDirection === 'asc' ? -1 : 1;
    }
    if (aValue > bValue) {
      return sortDirection === 'asc' ? 1 : -1;
    }
    return 0;
  });

  const getSortIcon = (field) => {
    if (sortField !== field) return '↕️';
    return sortDirection === 'asc' ? '↑' : '↓';
  };

  const getStatusBadge = (status) => {
    const statusColors = {
      'upcoming': '#28a745',
      'ongoing': '#ffc107',
      'past': '#6c757d'
    };

    return (
      <span 
        style={{
          padding: '4px 8px',
          borderRadius: '12px',
          fontSize: '12px',
          fontWeight: 'bold',
          color: 'white',
          backgroundColor: statusColors[status.toLowerCase()] || '#007bff'
        }}
      >
        {status}
      </span>
    );
  };

  if (tournaments.length === 0) {
    return (
      <div className="tournament-table-empty">
        <p>No tournaments found matching your criteria.</p>
      </div>
    );
  }

  return (
    <div className="tournament-table-container">
      <div className="tournament-table-wrapper">
        <table className="tournament-table">
          <thead>
            <tr>
              {/* Assessment Requirement: Table with creator, name, category, difficulty */}
              <th onClick={() => handleSort('creator')} className="sortable">
                Creator {getSortIcon('creator')}
              </th>
              <th onClick={() => handleSort('name')} className="sortable">
                Name {getSortIcon('name')}
              </th>
              <th onClick={() => handleSort('category')} className="sortable">
                Category {getSortIcon('category')}
              </th>
              <th onClick={() => handleSort('difficulty')} className="sortable">
                Difficulty {getSortIcon('difficulty')}
              </th>
              <th onClick={() => handleSort('status')} className="sortable">
                Status {getSortIcon('status')}
              </th>
              <th onClick={() => handleSort('startDate')} className="sortable">
                Start Date {getSortIcon('startDate')}
              </th>
              <th onClick={() => handleSort('endDate')} className="sortable">
                End Date {getSortIcon('endDate')}
              </th>
              <th>Min Score</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {sortedTournaments.map((tournament) => (
              <tr key={tournament.id}>
                <td>{tournament.createdBy?.username || 'Admin'}</td>
                <td>
                  {/* Assessment Requirement: Click tournament name to view questions */}
                  <button
                    onClick={() => onViewQuestions(tournament)}
                    className="tournament-name-link"
                    title="Click to view questions and answers"
                  >
                    {tournament.name}
                  </button>
                </td>
                <td>{tournament.category}</td>
                <td className="difficulty-cell">
                  {tournament.difficulty.charAt(0).toUpperCase() + tournament.difficulty.slice(1)}
                </td>
                <td>{getStatusBadge(tournament.status)}</td>
                <td>{new Date(tournament.startDate).toLocaleDateString()}</td>
                <td>{new Date(tournament.endDate).toLocaleDateString()}</td>
                <td>{tournament.minPassingScore}%</td>
                <td>
                  <div className="action-buttons">
                    <button
                      onClick={() => onEdit(tournament)}
                      className="btn-action btn-edit"
                      title="Edit tournament"
                    >
                      Edit
                    </button>
                    <button
                      onClick={() => onViewDetails(tournament)}
                      className="btn-action btn-view"
                      title="View details"
                    >
                      View
                    </button>
                    <button
                      onClick={() => onViewDetailedAnswers(tournament)}
                      className="btn-action btn-info"
                      title="View detailed user answers"
                    >
                      Answers
                    </button>
                    <button
                      onClick={() => onDelete(tournament.id, tournament.name)}
                      className="btn-action btn-delete"
                      title="Delete tournament"
                    >
                      Delete
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      
      <div className="tournament-table-footer">
        <p>Showing {tournaments.length} tournament{tournaments.length !== 1 ? 's' : ''}</p>
        <small>Click column headers to sort • Click tournament name to view questions</small>
      </div>
    </div>
  );
};

export default TournamentTable;