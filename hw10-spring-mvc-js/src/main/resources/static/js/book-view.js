document.addEventListener('DOMContentLoaded', function() {
    const api = new LibraryApi();
    const bookId = window.location.pathname.split('/').pop();

    loadBookDetails();
    loadComments();

    document.getElementById('addCommentBtn').addEventListener('click', function() {
        window.location.href = `/comments/create?bookId=${bookId}`;
    });

    async function loadBookDetails() {
        try {
            const book = await api.getBookById(bookId);
            document.getElementById('bookTitle').textContent = book.title || '';
            document.getElementById('bookAuthor').textContent = book.author?.fullName || '';
            document.getElementById('bookGenre').textContent = book.genre?.name || '';
            document.getElementById('editBookLink').href = `/books/edit/${book.id}`;
        } catch (error) {
            console.error('Error loading book details:', error);
        }
    }

    async function loadComments() {
        try {
            const comments = await api.getCommentsByBookId(bookId);
            renderCommentsTable(comments);
        } catch (error) {
            console.error('Error loading comments:', error);
        }
    }

    function renderCommentsTable(comments) {
        const tableContainer = document.getElementById('commentsTable');
        const noCommentsMessage = document.getElementById('noCommentsMessage');

        if (!comments || comments.length === 0) {
            tableContainer.style.display = 'none';
            noCommentsMessage.style.display = 'block';
            return;
        }

        noCommentsMessage.style.display = 'none';
        tableContainer.style.display = 'block';

        let tableHtml = `
            <table class="table table-striped table-hover align-middle">
                <thead>
                <tr>
                    <th scope="col">ID</th>
                    <th scope="col">Text</th>
                    <th scope="col">Actions</th>
                </tr>
                </thead>
                <tbody>
        `;

        comments.forEach(comment => {
            tableHtml += `
                <tr>
                    <td>${comment.id || ''}</td>
                    <td>${comment.text || ''}</td>
                    <td>
                        <a href="/comments/edit/${comment.id}" class="btn btn-outline-warning btn-sm me-2">Edit</a>
                        <button class="btn btn-outline-danger btn-sm" onclick="deleteComment('${comment.id}')">Delete</button>
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

    window.deleteComment = async function(id) {
        if (confirm('Are you sure you want to delete this comment?')) {
            try {
                await api.deleteComment(id);
                loadComments();
            } catch (error) {
                console.error('Error deleting comment:', error);
            }
        }
    };
});