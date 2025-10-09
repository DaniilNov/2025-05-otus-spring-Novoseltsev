document.addEventListener('DOMContentLoaded', function() {
    const api = new LibraryApi();
    loadAuthorsAndGenres();
    document.getElementById('createBookForm').addEventListener('submit', handleCreateBook);

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

    async function handleCreateBook(event) {
        event.preventDefault();

        const title = document.getElementById('title').value;
        const authorId = document.getElementById('authorId').value;
        const genreId = document.getElementById('genreId').value;

        try {
            const book = await api.createBook({
                title: title,
                authorId: authorId,
                genreId: genreId
            });
            window.location.href = `/books/view/${book.id}`;
        } catch (error) {
            console.error('Error creating book:', error);
            alert('Error creating book: ' + error.message);
        }
    }
});