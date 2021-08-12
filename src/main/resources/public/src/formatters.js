const currencyFormatter = new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' })

export const formatAmount = (value) => currencyFormatter.format(value)