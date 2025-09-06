document.addEventListener('DOMContentLoaded', function () {
    const api = new LibraryApi();

    const pathSegments = window.location.pathname.split('/').filter(segment => segment.length > 0);
    const commentId = pathSegments[pathSegments.length - 1];

    if (!commentId) {
        console.error('Comment ID not found in URL');
        return;
    }

    loadCommentForEdit(commentId);
    const form = document.getElementById('editCommentForm');
    if (form) {
        form.addEventListener('submit', function(event) {
            handleUpdateComment(event, commentId);
        });
    } else {
        console.error('Form element not found');
    }

    async function loadCommentForEdit(id) {
        try {
            console.log('Loading comment with ID:', id);
            const comment = await api.getCommentById(id);
            console.log('Received comment:', comment);

            const commentIdElement = document.getElementById('commentId');
            const bookIdElement = document.getElementById('bookId');
            const textElement = document.getElementById('text');
            const cancelLinkElement = document.getElementById('cancelLink');

            if (commentIdElement) commentIdElement.value = comment.id || '';
            if (bookIdElement) bookIdElement.value = comment.book?.id || '';
            if (textElement) textElement.value = comment.text || '';

            if (cancelLinkElement && comment.book?.id) {
                cancelLinkElement.href = `/books/view/${comment.book.id}`;
            }
        } catch (error) {
            console.error('Error loading comment:', error);
            alert('Failed to load comment: ' + (error.message || 'Unknown error'));
        }
    }

    async function handleUpdateComment(event, id) {
        event.preventDefault();
        console.log('Updating comment with ID:', id);

        const textElement = document.getElementById('text');
        const bookIdElement = document.getElementById('bookId');

        if (!textElement || !bookIdElement) {
            console.error('Required form elements not found');
            alert('Form is not properly initialized');
            return;
        }

        const text = textElement.value.trim();
        const bookId = bookIdElement.value.trim();

        if (!text) {
            alert('Comment text cannot be empty');
            return;
        }

        if (!bookId) {
            alert('Book ID is missing');
            return;
        }

        try {
            console.log('Sending update request:', { id, text });
            const updatedComment = await api.updateComment(id, { text: text });
            console.log('Update successful:', updatedComment);

            if (updatedComment?.book?.id) {
                window.location.href = `/books/view/${updatedComment.book.id}`;
            } else {
                window.location.href = '/books';
            }
        } catch (error) {
            console.error('Error updating comment:', error);
            alert('Error updating comment: ' + (error.message || 'Unknown error'));
        }
    }
});