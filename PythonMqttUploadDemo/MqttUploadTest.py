#!/usr/bin/env python
# -*- coding: utf-8 -*-
import threading
import time
import paho.mqtt.client as mqtt
import json

DefaultServerAddr = "iot.iotman.club"
DefaultServerPort = 1883
DefaultServerIfSsl = False
DefaultMqttHeartbreakInterval = 5 # mqtt心跳包发送间隔，单位为秒
DefaultDeviceId = "testpython"
DefaultUsername = "test"
DefaultUserPasswd = "test"
DefaultUploadTopic = "iotman/1512226921"
DefaultUploadInterval = 0.07 # 这里是上传时间间隔，单位是s，可以小数。0.07s=70ms

def TimerCallbackFun():
    """thread worker function"""
    print("send somthing")
    msg ="test"
    rc, mid = client.publish(DefaultUploadTopic, payload=msg, qos=0)
    on_publish(msg, rc)
    global timer
    timer = threading.Timer(DefaultUploadInterval, TimerCallbackFun)
    timer.start()

def on_publish(msg, rc):  # 成功发布消息的操作
    if rc == 0:
        print("publish success, msg = " + msg)

# 连接成功回调函数
def on_connect(client, userdata, flags, rc):
    print("Connected with result code " + str(rc))
    global timer
    timer = threading.Timer(DefaultUploadInterval, TimerCallbackFun)
    timer.start()
    # 连接完成之后订阅gpio主题
    # client.subscribe("gpio")


# 消息推送回调函数
# def on_message(client, userdata, msg):

    # print(msg.topic + " " + str(msg.payload))
    # revPayloadJson = json.loads(str(msg.payload))
    #    if revPayloadJson['value'] == 0:
    #       GPIO.output(gpio['pin'], GPIO.LOW)
    #    else:
    #       GPIO.output(gpio['pin'], GPIO.HIGH)

if __name__ == '__main__':
    client = mqtt.Client(client_id=DefaultDeviceId,userdata=None,protocol=mqtt.MQTTv311,clean_session=True,transport="tcp")
    client.on_connect = on_connect
    # client.on_message = on_message
    try:
        # 请根据实际情况改变MQTT代理服务器的IP地址
        client.username_pw_set(DefaultUsername, DefaultUserPasswd)
        client.connect(DefaultServerAddr, 1883, DefaultMqttHeartbreakInterval)
        client.loop_forever()
    except KeyboardInterrupt:
        timer.cancel()
        client.disconnect()
