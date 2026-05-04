import { defineStore } from 'pinia'
import { getWalletInfo, getWalletTransactions } from '@/api/wallet'

export const useWalletStore = defineStore('wallet', {
  state: () => ({
    walletInfo: null,
    transactions: [],
    loading: false
  }),

  getters: {
    balance: state => state.walletInfo?.balance || '0.00',
    frozenBalance: state => state.walletInfo?.frozen_balance || '0.00',
    availableBalance: state => {
      const balance = parseFloat(state.walletInfo?.balance || 0)
      const frozen = parseFloat(state.walletInfo?.frozen_balance || 0)
      return (balance - frozen).toFixed(2)
    }
  },

  actions: {
    async fetchWalletInfo() {
      this.loading = true
      try {
        const res = await getWalletInfo()
        this.walletInfo = res.data
        return res.data
      } catch (error) {
        console.error('Failed to fetch wallet info:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    async fetchTransactions(params = {}) {
      this.loading = true
      try {
        const res = await getWalletTransactions(params)
        this.transactions = res.data?.list || []
        return res.data
      } catch (error) {
        console.error('Failed to fetch transactions:', error)
        throw error
      } finally {
        this.loading = false
      }
    }
  }
})
