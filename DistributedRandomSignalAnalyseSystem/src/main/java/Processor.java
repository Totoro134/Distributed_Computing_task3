import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.util.*;

class NumberListener implements MessageListener {
    private int N = 0;
    private int cnt = 0;
    private double min = 0.0;
    private double max = 0.0;
    private boolean flag = true;
    private Queue<Double> que = new LinkedList<Double>();

    NumberListener(int N){
        this.N = N;
    }

    public void onMessage(Message message) {
        TextMessage textmessage = (TextMessage)message;
        try {
            double val = Double.valueOf(textmessage.getText());
            System.out.println("接受信号值：" + val);
            que.add(val);
            cnt++;

            if(flag) {
                min = val;
                max = val;
                flag = false;
            }else{
                min = min > val ? val : min;
                max = max > val ? max : val;
            }

            while(que.size() > N) {
                que.remove();
            }

            if(que.size() == N){
                double mean = 0, sigma = 0;
                for(double a:que){
                    mean += a;
                }
                mean /= N;
                for(double a:que){
                    sigma += (a - mean) * (a - mean);
                }
                sigma /= N;

                Publisher publisher = new Publisher("Result");
                publisher.sendResult(N, cnt, val, mean, sigma, min, max);
                Thread.sleep(2000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

public class Processor {
    public static void main(String[] args) throws JMSException {
        String brokerURL = "tcp://localhost:61616";
        ConnectionFactory factory = null;
        Connection connection = null;
        Session session = null;
        Destination destination = null;
        MessageConsumer messageConsumer = null;
        NumberListener listener = null;
        try {
            factory = new ActiveMQConnectionFactory(brokerURL);
            connection = factory.createConnection();
            connection.start();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            destination = session.createQueue("Number");
            messageConsumer = session.createConsumer(destination);
            System.out.println("请输入N:");
            Scanner scanner = new Scanner(System.in);
            int N = scanner.nextInt();
            listener = new NumberListener(N);
            messageConsumer.setMessageListener(listener);
            System.in.read();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connection.close();
        }
    }
}