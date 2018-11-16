package com.solace.apac.demo.stockmarket;

import com.solacesystems.jcsmp.*;

public class SubscriptionManager {
    // Solace related objects.
    private JCSMPProperties properties;
    private JCSMPSession session;
    private XMLMessageProducer prod;
    private Topic topic;
    private TextMessage txtMsg;
    private BytesMessage byteMsg;
    final String mType = "D";
    // Business logic related.
    final String MARKET_PREFIX = "TWSE";
    final String MARKET_STATUS_TOPIC = "MARKET_STATUS";
    private String marketStatusTopic;

    private void init() throws JCSMPException {
        // Create a JCSMP Session
        String host = "jj-solace1.mooo.com";
        String vpn_name = "test01";
        String username = "submanager01";
        String password = "password";

        this.properties = new JCSMPProperties();
        properties.setProperty(JCSMPProperties.HOST, host);
        properties.setProperty(JCSMPProperties.VPN_NAME, vpn_name);
        properties.setProperty(JCSMPProperties.USERNAME, username);
        properties.setProperty(JCSMPProperties.PASSWORD, password);

        // Connect to Solace
        this.session =  JCSMPFactory.onlyInstance().createSession(properties);

        // Activate a producer.
        // For direct messaging, these methods will never be invoked.
        this.prod = session.getMessageProducer(new JCSMPStreamingPublishEventHandler() {
            public void responseReceived(String messageID) {
                System.out.println("Producer received response for msg: " + messageID);
            }
            public void handleError(String messageID, JCSMPException e, long timestamp) {
                System.out.printf("Producer received error for msg: %s@%s - %s%n",
                        messageID,timestamp,e);
            }
        });

        this.txtMsg = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);
        this.byteMsg = JCSMPFactory.onlyInstance().createMessage(BytesMessage.class);
        //宣告系統或是全市場使用的topic
        marketStatusTopic = this.MARKET_PREFIX + "/" + this.MARKET_STATUS_TOPIC;
    }

    public void finish() {
        try {
            // 關閉所有連線
            this.session.closeSession();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    // 提供單個topic代理訂閱
    public void addSubscription(String clientName, String topicName) {
        ClientName cn = JCSMPFactory.onlyInstance().createClientName(clientName);
        Topic targetTopic = JCSMPFactory.onlyInstance().createTopic(topicName);

        try {
            this.session.addSubscription(cn, targetTopic, JCSMPSession.WAIT_FOR_CONFIRM);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // 提供單個topic代理解訂閱
    public void removeSubscription(String clientName, String topicName) {
        ClientName cn = JCSMPFactory.onlyInstance().createClientName(clientName);
        Topic targetTopic = JCSMPFactory.onlyInstance().createTopic(topicName);

        try {
            this.session.removeSubscription(cn, targetTopic, JCSMPSession.WAIT_FOR_CONFIRM);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // 提供多個topic代理訂閱
    public void addSubscription(String clientName, String[] topicNames) {
        ClientName cn = JCSMPFactory.onlyInstance().createClientName(clientName);
        Topic targetTopic;

        for (int i=0;i<topicNames.length;i++) {
            targetTopic = JCSMPFactory.onlyInstance().createTopic(topicNames[i]);
            try {
                this.session.addSubscription(cn, targetTopic, JCSMPSession.WAIT_FOR_CONFIRM);
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    // 提供多個topic代理解訂閱
    public void removeSubscription(String clientName, String[] topicNames) {
        ClientName cn = JCSMPFactory.onlyInstance().createClientName(clientName);
        Topic targetTopic;

        for (int i=0;i<topicNames.length;i++) {
            targetTopic = JCSMPFactory.onlyInstance().createTopic(topicNames[i]);
            try {
                this.session.removeSubscription(cn, targetTopic, JCSMPSession.WAIT_FOR_CONFIRM);
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public SubscriptionManager() {
        try {
            this.init();
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }
}
