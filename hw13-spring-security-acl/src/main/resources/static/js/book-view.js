document.addEventListener('DOMContentLoaded', function () {
    const api = new LibraryApi();
    const bookId = window.location.pathname.split('/').pop();

    window.deleteComment = async function (id) {
        if (confirm('Are you sure you want to delete this comment?')) {
            try {
                await api.deleteComment(id);
                loadComments();
            } catch (error) {
                console.error('Error deleting comment:', error);
                alert('Error deleting comment: ' + error.message);
            }
        }
    };

    loadBookDetails();
    loadComments();


    const currentUsernameElement = document.getElementById('currentUsername');
    const currentUsername = currentUsernameElement ? currentUsernameElement.value : null;
    console.log('Current username from page (in DOMContentLoaded):', currentUsername);

    const isAdminElement = document.getElementById('isAdminUser');

    const isAdminUser = isAdminElement ? isAdminElement.value === 'true' : false;
    console.log('Is current user admin (from page):', isAdminUser);

    document.getElementById('addCommentBtn').addEventListener('click', function () {
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
            const bookId = window.location.pathname.split('/').pop();
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

        console.log('Current username in renderCommentsTable:', currentUsername);
        console.log('Is admin in renderCommentsTable:', isAdminUser);

        let tableHtml = `
            <table class="table table-striped table-hover align-middle">
                <thead>
                <tr>
                    <th scope="col">ID</th>
                    <th scope="col">Text</th>
                    <th scope="col">Author</th>
                    <th scope="col">Actions</th>
                </tr>
                </thead>
                <tbody>
        `;

        comments.forEach(comment => {
            const canEditOrDelete = currentUsername && (comment.user?.username === currentUsername || isAdminUser);
            const authorName = comment.user?.username || 'Unknown';

            tableHtml += `
                <tr>
                    <td>${comment.id || ''}</td>
                    <td>${comment.text || ''}</td>
                    <td>${authorName}</td>
                    <td>
                        ${canEditOrDelete ? `<a href="/comments/edit/${comment.id}" class="btn btn-outline-warning btn-sm me-2">Edit</a>` : ''}
                        ${canEditOrDelete ? `<button class="btn btn-outline-danger btn-sm" onclick="deleteComment('${comment.id}')">Delete</button>` : ''}
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

});