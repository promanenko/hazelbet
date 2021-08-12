export const Header = {
    computed: {
        name() {
            return this.$store.state.user.data.name
        },
        balance() {
            return this.$store.state.user.data.balance
        }
    },
    template: `
        <header class="navbar navbar-expand-lg navbar-light bg-light mb-3">
            <div class="container">
                <div class="float-end">
                    {{ name }}
                    {{ balance }} $
                </div>
            </div>
        </header>
    `
}