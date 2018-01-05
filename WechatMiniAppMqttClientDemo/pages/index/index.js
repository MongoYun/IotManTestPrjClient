//index.js
//获取应用实例
const app = getApp()
var Paho = require('../../utils/mqttws31.js')
var client
Page({
  data: {
    revData:'',
    pubData:'',
    revTopic:'',
    userInfo: {},
    deviceId: '',
    userName: '',
    userPassword: '', 
    subTopic:'',
    hasUserInfo: false,
    canIUse: wx.canIUse('button.open-type.getUserInfo')
  },
  //事件处理函数
  bindViewTap: function() {
    wx.navigateTo({
      url: '../logs/logs'
    })
  },
  onLoad: function () {
    console.log("onLoad")
    this.setData({ subTopic: "iotman/1512226921", deviceId: "test", userName: "test", userPassword:"test"})
    if (app.globalData.userInfo) {
      this.setData({
        userInfo: app.globalData.userInfo,
        hasUserInfo: true
      })
    } else if (this.data.canIUse){
      // 由于 getUserInfo 是网络请求，可能会在 Page.onLoad 之后才返回
      // 所以此处加入 callback 以防止这种情况
      app.userInfoReadyCallback = res => {
        this.setData({
          userInfo: res.userInfo,
          hasUserInfo: true
        })
      }
    } else {
      // 在没有 open-type=getUserInfo 版本的兼容处理
      wx.getUserInfo({
        success: res => {
          app.globalData.userInfo = res.userInfo
          this.setData({
            userInfo: res.userInfo,
            hasUserInfo: true
          })
        }
      })
    }
  },
  getUserInfo: function(e) {
    console.log(e)
    app.globalData.userInfo = e.detail.userInfo
    this.setData({
      userInfo: e.detail.userInfo,
      hasUserInfo: true
    })
  }, 
  deviceIdInput: function (e) {
    this.setData({
      deviceId: e.detail.value
    })
  },
  userNameInput: function (e) {
    this.setData({
      userName: e.detail.value
    })
  },
  userPasswordInput: function (e) {
    this.setData({
      userPassword: e.detail.value
    })
    console.log(e.detail.value)
  },
  subTopicInput: function (e) {
    this.setData({
      subTopic: e.detail.value
    })
    console.log(e.detail.value)
  },
  publishMessage: function (topic,message,qos,retain) {
    var message = new Paho.Message(message);
    message.destinationName = topic;
    message.qos=qos
    message.retained = retain
    this.client.send(message);
  },
  pubDataInput: function (e) {
    this.setData({
      userName: e.detail.value
    })
  },
  pub:function(e){
    publishMessage(this.data.subTopic,this.data.pubData,1,false)
  },
  logIn:function (e) {
    //this.client.disconnect()
    this.client = new Paho.Client('iot.cnxel.cn', 8083, this.data.deviceId);
    console.log("mqtt connected")
    this.client.onMessageArrived = function (msg) {
      console.log("onMessageArrived:"+ msg.topic+":"+ msg.payloadString)
      this.setData({ revData: msg.payloadString, revTopic: msg.topic})
      /*wx.showToast({
          title: msg.payloadString
          
      });
      */
    }
    this.client.onConnectionLost = function (responseObject) {
      if (responseObject.errorCode !== 0) {
        console.log("onConnectionLost:" + responseObject.errorMessage)
      }
    }
    
    // connectOptions - Attributes used with the connection.
//     * @param {number} connectOptions.timeout - If the connect has not succeeded within this
//     * @param {string} connectOptions.userName - Authentication username for this connection.
//     * @param {string} connectOptions.password - Authentication password for this connection.
//     * @param {Paho.MQTT.Message} connectOptions.willMessage - sent by the server when the client
//     * @param {number} connectOptions.keepAliveInterval - the server disconnects this client if
//     * @param {boolean} connectOptions.cleanSession - if true(default) the client and server
//     * @param {boolean} connectOptions.useSSL - if present and true, use an SSL Websocket connection.
//     * @param {object} connectOptions.invocationContext - passed to the onSuccess callback or onFailure callback.
//     * @param {function} connectOptions.onSuccess - called when the connect acknowledgement
//     * <li>invocationContext as passed in to the onSuccess method in the connectOptions.
//     * @param {function} connectOptions.onFailure - called when the connect request has failed or timed out.
//     * <li>invocationContext as passed in to the onFailure method in the connectOptions.
//     * @param {array} connectOptions.hosts - If present this contains either a set of hostnames or fully qualified
//     * @param {array} connectOptions.ports - If present the set of ports matching the hosts. If hosts contains URIs, this property
//     * @param {boolean} connectOptions.reconnect - Sets whether the client will automatically attempt to reconnect
//     * @param {number} connectOptions.mqttVersion - The version of MQTT to use to connect to the MQTT Broker.
//     * @param {boolean} connectOptions.mqttVersionExplicit - If set to true, will force the connection to use the
//     * @param {array} connectOptions.uris - If present, should contain a list of fully qualified WebSocket uris
    
    this.client.connect({
            useSSL: false,
            cleanSession: false,
            keepAliveInterval: 30,
            //timeout:1*1000,
            userName: this.data.userName,
            password: this.data.userPassword,
            mqttVersion:4,
            onSuccess: function() {
                console.log('connected');
                this.client.subscribe(this.data.subTopic, {qos: 1});
            }
        });
    console.log('userName' + this.data.userName+this.data.subTopic);
  }
})
