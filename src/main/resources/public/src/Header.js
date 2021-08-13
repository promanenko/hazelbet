import { api } from './api.js'

export const Header = {
    computed: {
        name() {
            return this.$store.state.user.data?.userName ?? ''
        },
        balance() {
            return this.$store.state.user.data?.balance ?? ''
        },
    },
    created() {
        this.loadUser()
    },
    methods: {
        loadUser() {
            (async() => {
                const getUser = async() => {
                    const data = await api('/users/1')

                    this.setUser(data)

                    this.timer = setTimeout(getUser, 1000)
                }
                try {
                    getUser()
                } catch (e) {
                    console.log(e)
                }

            })()
        },
        ...Vuex.mapActions(['setUser'])
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