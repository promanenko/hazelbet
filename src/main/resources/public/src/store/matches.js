import { api } from '../api.js'

/*{
    "id": 1,
    "firstTeam": "Barcelona",
    "secondTeam": "Madrid",
    "winFirst": 2.1,
    "draw": 2.2,
    "winSecond": 1.7
}*/
export const matches = {
    state:() => ({
        data: [],
        error: null,
        loading: false
    }),
    mutations: {
        setMatches(state, data) {
            state.data = data
        },
        setLoading(state, data) {
            state.loadin = data
        }
    },
    actions: {
        getMatches : async ({ commit, dispatch }) => {
            dispatch('setLoading', true)
            const data = await api('/matches')
            commit('setMatches', data)
            dispatch('setLoading', false)
        },
        setLoading({commit}, loading) {
            commit('setLoading', loading)
        }
    }
}