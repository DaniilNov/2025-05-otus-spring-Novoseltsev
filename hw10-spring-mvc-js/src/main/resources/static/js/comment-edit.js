document.addEventListener('DOMContentLoaded', function () {
    const api = new LibraryApi();
    const pathParts = window.location.pathname.split('/');
    const commentId = pathParts.pop();
    const bookId = pathParts[pathParts.length - 2];

    loadCommentForEdit();
    document.getElementById('editCommentForm').addEventListener('submit', handleUpdateComment);

    async function loadCommentForEdit() {
        try {
            const comment = await api.getCommentById(bookId, commentId);
            document.getElementById('commentId').value = comment.id;
            document.getElementById('bookId').value = comment.book.id;
            document.getElementById('text').value = comment.text;

            if (comment.book && comment.book.id) {
                document.getElementById('cancelLink').href = `/books/view/${comment.book.id}`;
            }
        } catch (error) {
            console.error('Error loading comment:', error);
        }
    }

    async function handleUpdateComment(event) {
        event.preventDefault();

        const text = document.getElementById('text').value;
        const commentId = document.getElementById('commentId').value;
        const bookId = document.getElementById('bookId').value;

        try {
            const comment = await api.updateComment(bookId, commentId, {
                text: text
            });
            if (comment.book && comment.book.id) {
                window.location.href = `/books/view/${comment.book.id}`;
            } else {
                window.location.href = '/books';
            }
        } catch (error) {
            console.error('Error updating comment:', error);
            alert('Error updating comment: ' + error.message);
        }
    }
});