class LibraryApi {
    constructor() {
        this.baseUrl = '/api/v1';
    }

    async fetchJson(url, options = {}) {
        const response = await fetch(url, {
            headers: {
                'Content-Type': 'application/json',
                ...options.headers
            },
            ...options
        });
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.json();
    }

    async getAllBooks() {
        return this.fetchJson(`${this.baseUrl}/books`);
    }

    async getBookById(id) {
        return this.fetchJson(`${this.baseUrl}/books/${id}`);
    }

    async createBook(bookData) {
        return this.fetchJson(`${this.baseUrl}/books`, {
            method: 'POST',
            body: JSON.stringify(bookData)
        });
    }

    async updateBook(id, bookData) {
        return this.fetchJson(`${this.baseUrl}/books/${id}`, {
            method: 'PUT',
            body: JSON.stringify(bookData)
        });
    }

    async deleteBook(id) {
        const response = await fetch(`${this.baseUrl}/books/${id}`, {
            method: 'DELETE'
        });
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response;
    }

    async getAllAuthors() {
        return this.fetchJson(`${this.baseUrl}/authors`);
    }

    async getAllGenres() {
        return this.fetchJson(`${this.baseUrl}/genres`);
    }

    async getCommentsByBookId(bookId) {
        return this.fetchJson(`${this.baseUrl}/comments/book/${bookId}`);
    }

    async getCommentById(id) {
        return this.fetchJson(`${this.baseUrl}/comments/${id}`);
    }

    async createComment(commentData) {
        return this.fetchJson(`${this.baseUrl}/comments`, {
            method: 'POST',
            body: JSON.stringify(commentData)
        });
    }

    async updateComment(id, commentData) {
        return this.fetchJson(`${this.baseUrl}/comments/${id}`, {
            method: 'PUT',
            body: JSON.stringify(commentData)
        });
    }

    async deleteComment(id) {
        const response = await fetch(`${this.baseUrl}/comments/${id}`, {
            method: 'DELETE'
        });
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response;
    }
}

const api = new LibraryApi();