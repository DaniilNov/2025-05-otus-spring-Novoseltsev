document.addEventListener('DOMContentLoaded', function() {
    const api = new LibraryApi();
    const bookId = window.location.pathname.split('/').pop();

    loadBookForEdit();
    loadAuthorsAndGenres();
    document.getElementById('editBookForm').addEventListener('submit', handleUpdateBook);
    document.getElementById('cancelLink').href = `/books/view/${bookId}`;

    async function loadBookForEdit() {
        try {
            const book = await api.getBookById(bookId);
            document.getElementById('bookId').value = book.id;
            document.getElementById('title').value = book.title;

            setTimeout(() => {
                if (book.author) {
                    document.getElementById('authorId').value = book.author.id;
                }
                if (book.genre) {
                    document.getElementById('genreId').value = book.genre.id;
                }
            }, 100);
        } catch (error) {
            console.error('Error loading book:', error);
        }
    }

    async function loadAuthorsAndGenres() {
        try {
            const authors = await api.getAllAuthors();
            const genres = await api.getAllGenres();

            const authorSelect = document.getElementById('authorId');
            authors.forEach(author => {
                const option = document.createElement('option');
                option.value = author.id;
                option.textContent = author.fullName;
                authorSelect.appendChild(option);
            });

            const genreSelect = document.getElementById('genreId');
            genres.forEach(genre => {
                const option = document.createElement('option');
                option.value = genre.id;
                option.textContent = genre.name;
                genreSelect.appendChild(option);
            });
        } catch (error) {
            console.error('Error loading data:', error);
        }
    }

    async function handleUpdateBook(event) {
        event.preventDefault();

        const title = document.getElementById('title').value;
        const authorId = document.getElementById('authorId').value;
        const genreId = document.getElementById('genreId').value;

        try {
            const book = await api.updateBook(bookId, {
                title: title,
                authorId: authorId,
                genreId: genreId
            });
            window.location.href = `/books/view/${book.id}`;
        } catch (error) {
            console.error('Error updating book:', error);
            alert('Error updating book: ' + error.message);
        }
    }
});