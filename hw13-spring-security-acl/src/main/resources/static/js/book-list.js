document.addEventListener('DOMContentLoaded', function() {
    const api = new LibraryApi();
    loadBooks();

    async function loadBooks() {
        try {
            const books = await api.getAllBooks();
            renderBooksTable(books);
        } catch (error) {
            console.error('Error loading books:', error);
        }
    }

    function renderBooksTable(books) {
        const tableContainer = document.getElementById('booksTable');
        const noBooksMessage = document.getElementById('noBooksMessage');

        if (!books || books.length === 0) {
            tableContainer.style.display = 'none';
            noBooksMessage.style.display = 'block';
            return;
        }
        noBooksMessage.style.display = 'none';
        tableContainer.style.display = 'block';

        const currentUserRoleElement = document.getElementById('currentUserRole');
        const currentUserRole = currentUserRoleElement ? currentUserRoleElement.value : null;
        console.log('Current user role from page:', currentUserRole);

        let tableHtml = `
        <table class="table table-striped table-hover align-middle">
            <thead class="table-dark">
                <tr>
                    <th scope="col">ID</th>
                    <th scope="col">Title</th>
                    <th scope="col">Author</th>
                    <th scope="col">Genre</th>
                    <th scope="col">Actions</th>
                </tr>
            </thead>
            <tbody>
    `;

        books.forEach(book => {
            const isAdmin = currentUserRole === 'ADMIN';
            console.log('Is admin for book ' + book.id + ':', isAdmin);

            tableHtml += `
            <tr>
                <td>${book.id || ''}</td>
                <td>${book.title || ''}</td>
                <td>${book.author?.fullName || ''}</td>
                <td>${book.genre?.name || ''}</td>
                <td>
                    <a href="/books/view/${book.id}" class="btn btn-info btn-sm me-1">View</a>
                    ${isAdmin ? `<a href="/books/edit/${book.id}" class="btn btn-warning btn-sm me-1">Edit</a>` : ''}
                    ${isAdmin ? `<button class="btn btn-danger btn-sm" onclick="deleteBook('${book.id}')">Delete</button>` : ''}
                </td>
            </tr>
        `;
        });

        tableHtml += `
            </tbody>
        </table>
    `;

        tableContainer.innerHTML = tableHtml;
    }

    window.deleteBook = async function(id) {
        if (confirm('Are you sure you want to delete this book?')) {
            try {
                await api.deleteBook(id);
                loadBooks();
            } catch (error) {
                console.error('Error deleting book:', error);
            }
        }
    };
});