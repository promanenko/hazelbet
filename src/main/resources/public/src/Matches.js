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
      ...Vuex.mapActions(['getMatches'])
    },
    created() {
        this.getMatches()
    },
    template: `
        <spinner v-if="loading" />
        <table class="table table-striped" v-if="!loading">
            <thead>
                <tr>
                    <th>Match</th>
                    <th>W1</th>
                    <th>X</th>
                    <th>W2</th>
                </tr>
            </thead>
            <tbody>
                <tr v-for="(data, index) in items" :key="index">
                  <td>{{ data.firstTeam }} - {{ data.secondTeam }}</td>
                  <td>{{ data.winFirst }}</td>
                  <td>{{ data.draw }}</td>
                  <td>{{ data.winSecond }}</td>
                </tr>
            </tbody>
        </table>
    `
}