/*{
    draw: 2.2
    drawTrend: "STABLE"
    firstScored: 0
    firstTeam: "Barcelona"
    id: 1
    secondScored: 0
    secondTeam: "Madrid"
    winFirst: 2.1
    winFirstTrend: "STABLE"
    winSecond: 1.7
    winSecondTrend: "STABLE
}*/
export const matches = {
    state:() => ({
        data: [],
    }),
    mutations: {
        setMatches(state, data) {
            state.data = data
        },
    },
    actions: {
        setMatches({ commit }, data) {
            commit('setMatches', data)
        }
    },
    getters: {
        getMatchById: (state) => (matchId) => {
            if (!matchId) {
                return null
            }

            return state.data.find(item => item.id === matchId)
        }
    }
}