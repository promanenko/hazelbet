export const Overlay = {
    props: ['onClose'],
    template: `
        <div class="overlay">
            <div class="overlay-content">
                <div>
                    <slot name="content" ></slot>
                </div>
                <footer class="overlay-footer">
                    <slot name="footer" ></slot>
                    <button type="button" @click="onClose" class="btn btn-danger">Cancel</button>
                </footer>
            </div>
        </div>
    `
}