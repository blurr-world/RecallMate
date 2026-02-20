# RecallMate

RecallMate is an Android application designed to help users study and revise topics more effectively. The app allows you to generate summaries from text and then create interactive multiple-choice question (MCQ) quizzes based on those summaries. This process helps reinforce learning through active recall.

## Features

- **Generate Summaries:** Create concise summaries from longer texts to focus on key information.
- **Create MCQ Quizzes:** Automatically generate interactive MCQ quizzes from your summaries to test your knowledge.
- **Interactive Practice:** Take quizzes in a dedicated practice mode with features like question navigation and instant feedback.
- **Track Your Progress:** View your score and percentage at the end of each quiz to track your performance.
- **Modern UI:** The app is built with Material Design components for a clean and intuitive user experience.

## How to Use

### 1. Create a Summary

1.  Navigate to the "Create" tab.
2.  Input the text you want to summarize.
3.  Click the "Generate Summary" button to create a summary.

### 2. Create an MCQ Quiz

1.  Navigate to the "Create MCQ" screen.
2.  Select a summary from your library.
3.  Configure the quiz settings, including the number of questions and difficulty level.
4.  Click "Generate Quiz" to create a new set of MCQs.

### 3. Practice a Quiz

1.  After generating a quiz, you will be automatically navigated to the practice screen.
2.  Answer each question by selecting one of the options.
3.  Use the "Next" and "Previous" buttons to navigate between questions.
4.  On the last question, a "Submit" button will appear. Click it to view your results.
5.  If you try to exit before submitting, a warning dialog will appear to prevent accidental loss of progress.

### 4. View Your Results

1.  After submitting, a results screen will appear, showing your score and the percentage of correct answers.
2.  Click "Exit Practice" to return to the main screen.

## Project Structure

- **`:app` module:** This is the main application module containing all the source code.
- **`core` package:** Contains core components like models (`McqModel`, `SummaryModel`), utility functions, and other shared resources.
- **`mcq` package:** This package holds all the code related to the MCQ feature, including:
    - `ui`: Contains the `CreateMCQFragment` and `PracticeMCQFragment`.
    - `repository`: Manages data operations for MCQs.
    - `viewmodel`: Provides data to the UI and handles user interactions.
- **Other packages:** The project is organized by feature, with packages for `dashboard`, `upload`, `chat`, `settings`, and `library`.