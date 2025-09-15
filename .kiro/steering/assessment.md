# Assessment Requirements & Criteria

## Project Overview
This is an academic assessment focusing on **frontend-backend decoupling** using React frontend and Spring Boot backend as separate applications communicating via REST APIs.

## Key Assessment Criteria

### 1. Functionality & Robustness (Learning Outcomes 1, 2)

#### Admin UI Requirements (Must Implement)
- ✅ **Create Tournament**: Modal form with creator, name, category, difficulty fields
- ✅ **Form Validation**: Graceful error handling (e.g., blank creator field validation)
- ✅ **View Tournaments**: Table display with creator, name, category, difficulty columns
- ✅ **Update Tournament**: Modal form for editing existing tournaments
- ✅ **Delete Tournament**: User confirmation prompt before deletion
- ✅ **View Questions**: Click tournament name to see questions, correct/incorrect answers

#### Player UI Requirements (Must Implement)
- ✅ **Browse Tournaments**: View available tournaments for participation
- ✅ **Play Tournaments**: Interactive quiz gameplay interface
- ✅ **Track Progress**: Display completion status and performance metrics

#### Technical Requirements
- ✅ **Responsive Design**: Visually attractive and mobile-friendly UI
- ✅ **Dependency Management**: Proper npm/Maven configuration
- ✅ **Error Handling**: Robust validation and graceful error management
- ✅ **API Integration**: RESTful communication between decoupled services

### 2. Testing Requirements
- **Tournament CRUD Testing**: Create, update, delete operations
- **Question Viewing Tests**: Verify question display functionality
- **Form Validation Tests**: Test error handling and validation messages
- **UI Component Tests**: Modal interactions, table operations

### 3. Documentation & Git Usage
- **Frontend Repository**: Separate git repository for React application
- **Commit Messages**: Must reflect functional requirement changes
- **Documentation**: README with setup instructions and screenshots
- **Code Comments**: Clear documentation of complex logic

### 4. Design Patterns & Best Practices
- **Component Architecture**: Reusable React components with single responsibility
- **State Management**: Proper use of React Context for global state
- **API Layer**: Centralized service layer for backend communication
- **Error Boundaries**: Graceful error handling in React components
- **Form Handling**: Controlled components with validation

## Submission Requirements
- Complete project with documentation
- Screenshots demonstrating functionality
- Reflection report (individual submission)
- Evidence of proper git usage with meaningful commits

## Pass Criteria
- Cumulative pass mark of 50%
- All functional requirements implemented
- Proper frontend-backend decoupling demonstrated
- Code quality and best practices followed

## Academic Integrity
- All code must be original work
- Proper citation of any external references
- Individual reflection report required (even for pair projects)