document.addEventListener('DOMContentLoaded', function() {
    const api = new LibraryApi();

    const urlParams = new URLSearchParams(window.location.search);
    const bookId = urlParams.get('bookId');

    if (bookId) {
        document.getElementById('bookId').value = bookId;
        document.getElementById('cancelLink').href = `/books/view/${bookId}`;
    }

    document.getElementById('createCommentForm').addEventListener('submit', handleCreateComment);

    async function handleCreateComment(event) {
        event.preventDefault();

        const text = document.getElementById('text').value;
        const bookId = document.getElementById('bookId').value;

        try {
            await api.createComment({
                text: text,
                bookId: bookId
            });
            window.location.href = `/books/view/${bookId}`;
        } catch (error) {
            console.error('Error creating comment:', error);
            alert('Error creating comment: ' + error.message);
        }
    }
});