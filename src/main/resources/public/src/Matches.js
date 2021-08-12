import { api } from './api.js'

export const Matches = {
    computed: {
        items() {
            return this.$store.state.matches.data
        },
        loading() {
            return this.$store.state.matches.loading
        },
        loaded() {
            return !this.$store.state.matches.loading
        }
    },
    methods: {
        getMatches() {
            (async() => {
                const loadMatches = async() => {
                    const data = await api('/matches')

                    this.setMatches(data)

                    this.timer = setTimeout(loadMatches, 1000)
                }
                try {
                    loadMatches()
                } catch (e) {
                    console.log(e)
                }

            })()
        },
      ...Vuex.mapActions(['setMatches'])
    },
    created() {
        this.getMatches()
    },
    unmounted() {
        clearTimeout(this.timer)
    },
    template: `
        <spinner v-if="loading" />
        <table class="table table-striped" v-if="!loading">
            <thead>
                <tr>
                    <th>Match</th>
                    <th>Score</th>
                    <th>W1</th>
                    <th>X</th>
                    <th>W2</th>
                </tr>
            </thead>
            <tbody>
                <tr v-if="items.length === 0"><td class="text-center" colspan="7">No data</td></tr>
                <match-row v-for="(data, index) in items" :key="data.id" :data="data" />
            </tbody>
        </table>
    `
}