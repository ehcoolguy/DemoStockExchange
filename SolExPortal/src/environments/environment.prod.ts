export const environment = {
  production: true,
  solace: {
    host: 'https://my-solace.mooo.com',
    vpn: 'test01',
    clientUserName: 'guest-mobile',
    clientPassword: 'password'
  },
  restBackend: {
    uri: 'http://my-solace.mooo.com:8080/api/general/listStocks'
  }
};
