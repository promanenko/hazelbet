export const user = {
    state:() => ({
        data: null,
        error: null,
        loading: false
    }),
    mutations: {
        setUser(state, data) {
            state.data = data
        }
    },
    actions: {
        setUser({ commit }, data) {
            commit('setUser', data)
        }
    }
}