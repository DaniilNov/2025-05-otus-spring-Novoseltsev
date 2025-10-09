document.addEventListener('DOMContentLoaded', function () {
    const api = new LibraryApi();
    const pathSegments = window.location.pathname.split('/').filter(segment => segment);
    const commentId = pathSegments[pathSegments.length - 1];

    if (!commentId) {
        alert("Unable to load comment: Invalid URL.");
        return;
    }

    loadCommentForEdit();
    document.getElementById('editCommentForm').addEventListener('submit', handleUpdateComment);

    async function loadCommentForEdit() {
        try {
            const comment = await api.getCommentById(commentId);

            if (!comment) {
                throw new Error("Comment data is empty");
            }

            document.getElementById('commentId').value = comment.id || '';
            const bookId = comment.book?.id;
            if (bookId) {
                let bookIdInput = document.getElementById('bookId');
                if (!bookIdInput) {
                    bookIdInput = document.createElement('input');
                    bookIdInput.type = 'hidden';
                    bookIdInput.id = 'bookId';
                    bookIdInput.name = 'bookId';
                    document.getElementById('editCommentForm').appendChild(bookIdInput);
                }
                bookIdInput.value = bookId;
            }
            document.getElementById('text').value = comment.text || '';

            if (bookId) {
                document.getElementById('cancelLink').href = `/books/view/${bookId}`;
            } else {
                document.getElementById('cancelLink').href = '/books';
            }
        } catch (error) {
            console.error('Error loading comment:', error);
            alert('Error loading comment: ' + (error.message || 'Unknown error'));
        }
    }

    async function handleUpdateComment(event) {
        event.preventDefault();

        const text = document.getElementById('text').value.trim();
        const formCommentId = document.getElementById('commentId').value;
        const bookIdInput = document.getElementById('bookId');
        const bookId = bookIdInput ? bookIdInput.value : null;

        if (!text) {
            alert('Comment text cannot be empty.');
            return;
        }
        if (!formCommentId || formCommentId !== commentId) {
            alert('Error: Comment ID mismatch.');
            return;
        }

        try {
            const updatedComment = await api.updateComment(commentId, {
                text: text
            });

            if (updatedComment?.book?.id) {
                window.location.href = `/books/view/${updatedComment.book.id}`;
            } else if(bookId) {
                window.location.href = `/books/view/${bookId}`;
            } else {
                window.location.href = '/books';
            }
        } catch (error) {
            console.error('Error updating comment:', error);
            if (error.message && error.message.includes('403')) {
                alert('Error updating comment: Access forbidden. You might need to reload the page and try again. (CSRF token issue)');
            } else {
                alert('Error updating comment: ' + (error.message || 'Unknown error'));
            }
        }
    }
});