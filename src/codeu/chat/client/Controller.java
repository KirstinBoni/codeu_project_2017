// Copyright 2017 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package codeu.chat.client;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread;

import codeu.chat.common.BasicController;
import codeu.chat.common.Conversation;
import codeu.chat.common.Message;
import codeu.chat.common.NetworkCode;
import codeu.chat.common.User;
import codeu.chat.common.Uuid;
import codeu.chat.common.Uuids;
import codeu.chat.util.Logger;
import codeu.chat.util.Serializers;
import codeu.chat.util.connections.Connection;
import codeu.chat.util.connections.ConnectionSource;

public class Controller implements BasicController {

  private final static Logger.Log LOG = Logger.newLog(Controller.class);

  private final ConnectionSource source;

  public Controller(ConnectionSource source) {
    this.source = source;
  }

  @Override
  public Message newMessage(Uuid author, Uuid conversation, String body) {

    Message response = null;

    try (final Connection connection = source.connect()) {

      Serializers.INTEGER.write(connection.out(), NetworkCode.NEW_MESSAGE_REQUEST);
      Uuids.SERIALIZER.write(connection.out(), author);
      Uuids.SERIALIZER.write(connection.out(), conversation);
      Serializers.STRING.write(connection.out(), body);

      if (Serializers.INTEGER.read(connection.in()) == NetworkCode.NEW_MESSAGE_RESPONSE) {
        response = Serializers.nullable(Message.SERIALIZER).read(connection.in());
      } else {
        LOG.error("Response from server failed.");
      }
    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return response;
  }

  @Override
  public User newUser(String name) {

    User response = null;

    try (final Connection connection = source.connect()) {

      Serializers.INTEGER.write(connection.out(), NetworkCode.NEW_USER_REQUEST);
      Serializers.STRING.write(connection.out(), name);
      LOG.info("newUser: Request completed.");

      if (Serializers.INTEGER.read(connection.in()) == NetworkCode.NEW_USER_RESPONSE) {
        response = Serializers.nullable(User.SERIALIZER).read(connection.in());
        LOG.info("newUser: Response completed.");
      } else {
        LOG.error("Response from server failed.");
      }
    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return response;
  }

  @Override
  public Conversation newConversation(String title, Uuid owner)  {

    Conversation response = null;

    try (final Connection connection = source.connect()) {

      Serializers.INTEGER.write(connection.out(), NetworkCode.NEW_CONVERSATION_REQUEST);
      Serializers.STRING.write(connection.out(), title);
      Uuids.SERIALIZER.write(connection.out(), owner);

      if (Serializers.INTEGER.read(connection.in()) == NetworkCode.NEW_CONVERSATION_RESPONSE) {
        response = Serializers.nullable(Conversation.SERIALIZER).read(connection.in());
      } else {
        LOG.error("Response from server failed.");
      }
    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return response;
  }
  
  @Override 
  public void deleteUser(String user){
	  
	  Conversation response = null;

	  try (final Connection connection = source.connect()) {
	      Serializers.INTEGER.write(connection.out(), NetworkCode.DELETE_USER_REQUEST);
	      Serializers.STRING.write(connection.out(), user);
	    
	      if (Serializers.INTEGER.read(connection.in()) == NetworkCode.DELETE_USER_RESPONSE) {
	          LOG.info("Delete user: %s success", user);
	        } else {
	          LOG.error("Response from server failed.");
	        }
	  
	  } catch (Exception ex) {
	    System.out.println("ERROR: Exception during call on server. Check log for details.");
	    LOG.error(ex, "Exception during call on server.");
	    
	  }
  }
  
  @Override 
  public void deleteConversation(String conversation){
	  
	  Conversation response = null;

	  try (final Connection connection = source.connect()) {
	      Serializers.INTEGER.write(connection.out(), NetworkCode.DELETE_CONVERSATION_REQUEST);
	      Serializers.STRING.write(connection.out(), conversation);
	    
	      if (Serializers.INTEGER.read(connection.in()) == NetworkCode.DELETE_CONVERSATION_RESPONSE) {
	          LOG.info("Delete conversation: %s success", conversation);
	        } else {
	          LOG.error("Response from server failed.");
	        }
	  
	  } catch (Exception ex) {
	    System.out.println("ERROR: Exception during call on server. Check log for details.");
	    LOG.error(ex, "Exception during call on server.");
	    
	  }
  }
  
	@Override
  public void deleteMessage(String body)
  {
    Conversation response = null;

    try(final Connection connection = source.connect())
    { 
      Serializers.INTEGER.write(connection.out(), NetworkCode.DELETE_MESSAGE_REQUEST);
      Serializers.STRING.write(connection.out(), body);

      if(Serializers.INTEGER.read(connection.in()) == NetworkCode.DELETE_MESSAGE_RESPONSE){
        LOG.info("Delete message: %s success", body);
      } else {
        LOG.error("Response from server failed.");
      }
    }
    catch(Exception e)
    {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(e, "Exception during call on server.");
    }
  }
}
