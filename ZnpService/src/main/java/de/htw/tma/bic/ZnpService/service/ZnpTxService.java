package de.htw.tma.bic.ZnpService.service;


import de.htw.tma.bic.UsbDriver.UsbDriver;

import static de.htw.tma.bic.ZnpService.service.ZnpCodes.*;


public class ZnpTxService {

    private UsbDriver driver;
    
    ZnpTxService(UsbDriver driver){
        this.driver = driver;
    }

    // TESTING
    public void sendATrial(byte[] message) {
        message[0] = (byte) 254; // SOP
        message[message.length - 1] = calcFCS(message);
        sendMessage(message);
    }

    // DEVICE
    public void resetDevice() {
        sendMessage(generateMessage(bc, bc_reset, new byte[0]));
    }

    public void deviceInfo() {
        byte[] message = generateMessage(util, util_deviceInfo, null);
        sendMessage(message);
    }

    public void requestNetworkStatus() {
        sendMessage(generateMessage(bc, bc_info, new byte[0]));
    }

    // CHAT
    public void sendChatMsg(String messageBody) {

        byte[] message = messageBody.getBytes();

        sendMessage(generateMessage(bc, bc_sendToAll, message));
    }

    public void sendChatMsg(byte[] ip16, String messageBody) {

        byte[] messageBytes = messageBody.getBytes();
        byte[] message = new byte[ip16.length + messageBytes.length];
        int index = 0;
        for ( byte b : ip16) message[index++] = b;
        for ( byte b : messageBytes) message[index++] = b;

        sendMessage(generateMessage(bc, bc_sendToOne, message));
    }

    public void sendCommand(byte[] ip16, byte cmd0, byte cmd1, byte[] messageBody){
        byte[] message = new byte[messageBody.length + 5];
        int index = 0;
        message[index++] = ip16[0];
        message[index++] = ip16[1];
        message[index++] = (byte) messageBody.length;
        message[index++] = cmd0;
        message[index++] = cmd1;

        for (byte b:messageBody) message[index++] = b;
        sendMessage(generateMessage(bc, bc_sendCommand, message));

    }

    // NETWORK
   public void startCoordinator() {
        sendMessage(generateMessage(bc, bc_startCoord, new byte[0]));
    }

    public void startRouter() {
        sendMessage(generateMessage(bc, bc_startRouter, new byte[0]));
    }

    public void setChannels(Channels channelMask){
        sendMessage(generateMessage(bc, bc_setChannels, channelMask.value()));
    }


    // PRIVATE METHODS


    private void sendMessage(byte[] message) {

        driver.sendUsbData(message);
    }

    private byte[] generateMessage(int cmd0, int cmd1, byte[] data) {

        int dataLength;
        if (data == null)
            dataLength = 0;
        else
            dataLength = data.length;

        byte[] tmpBytes = new byte[dataLength + 5];
        int pointer = 0;
        tmpBytes[pointer++] = (byte) 254; // SOP
        tmpBytes[pointer++] = (byte) dataLength;
        tmpBytes[pointer++] = (byte) cmd0;
        tmpBytes[pointer++] = (byte) cmd1;

        if (data != null) {
            for (byte dataByte : data) {
                tmpBytes[pointer++] = dataByte;
            }
        }
        tmpBytes[pointer] = calcFCS(tmpBytes);
        return tmpBytes;
    }

    private byte calcFCS(byte[] bytes) {
        byte xorResult;
        xorResult = 0;
        for (int i = 1; i < bytes.length - 1; i++) {
            xorResult = (byte) (xorResult ^ bytes[i]);
        }
        return xorResult;
    }

}
