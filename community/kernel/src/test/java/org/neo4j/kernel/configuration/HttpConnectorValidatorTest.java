/*
 * Copyright (c) 2002-2016 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.neo4j.kernel.configuration;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.neo4j.graphdb.config.InvalidSettingException;
import org.neo4j.kernel.configuration.HttpConnector.Encryption;

import static java.util.Collections.emptyMap;
import static org.junit.Assert.assertEquals;
import static org.neo4j.helpers.collection.MapUtil.stringMap;
import static org.neo4j.kernel.configuration.Connector.ConnectorType.HTTP;

public class HttpConnectorValidatorTest
{
    HttpConnectorValidator cv = new HttpConnectorValidator();

    @Rule
    public ExpectedException expected = ExpectedException.none();

    @Test
    public void doesNotValidateUnrelatedStuff() throws Exception
    {
        assertEquals( 0, cv.validate( stringMap( "dbms.connector.bolt.enabled", "true",
                "dbms.blabla.boo", "123" ) ).size() );
    }

    @Test
    public void onlyEnabledRequiredWhenNameIsHttpOrHttps() throws Exception
    {
        String httpEnabled = "dbms.connector.http.enabled";
        String httpsEnabled = "dbms.connector.https.enabled";

        assertEquals( stringMap( httpEnabled, "true" ),
                cv.validate( stringMap( httpEnabled, "true" ) ) );

        assertEquals( stringMap( httpsEnabled, "true" ),
                cv.validate( stringMap( httpsEnabled, "true" ) ) );
    }

    @Test
    public void requiresTypeWhenNameIsNotHttpOrHttps() throws Exception
    {
        String randomEnabled = "dbms.connector.bla.enabled";
        String randomType = "dbms.connector.bla.type";

        assertEquals( stringMap( randomEnabled, "true", randomType, HTTP.name() ),
                cv.validate( stringMap( randomEnabled, "true", randomType, HTTP.name() ) ) );

        expected.expect( InvalidSettingException.class );
        expected.expectMessage( "Missing mandatory value for 'dbms.connector.bla.type'" );

        cv.validate( stringMap( randomEnabled, "true" ) );
    }

    @Test
    public void validatesEncryption() throws Exception
    {
        String key = "dbms.connector.bla.encryption";
        String type = "dbms.connector.bla.type";

        assertEquals( stringMap( key, Encryption.NONE.name(),
                type, HTTP.name() ),
                cv.validate( stringMap( key, Encryption.NONE.name(), type, HTTP.name() ) ) );

        assertEquals( stringMap( key, Encryption.TLS.name(),
                type, HTTP.name() ),
                cv.validate( stringMap( key, Encryption.TLS.name(), type, HTTP.name() ) ) );

        expected.expect( InvalidSettingException.class );
        expected.expectMessage(
                "Bad value 'BOBO' for setting 'dbms.connector.bla.encryption': must be one of [NONE, TLS] case " +
                        "sensitive" );

        cv.validate( stringMap( key, "BOBO", type, HTTP.name() ) );
    }

    @Test
    public void httpsConnectorCanOnlyHaveTLS() throws Exception
    {
        String key = "dbms.connector.https.encryption";

        assertEquals( stringMap( key, Encryption.TLS.name() ),
                cv.validate( stringMap( key, Encryption.TLS.name() ) ) );

        expected.expect( InvalidSettingException.class );
        expected.expectMessage(
                "'dbms.connector.https.encryption' is only allowed to be 'TLS'; not 'NONE'" );
        cv.validate( stringMap( key, Encryption.NONE.name() ) );
    }

    @Test
    public void httpConnectorCanNotHaveTLS() throws Exception
    {
        String key = "dbms.connector.http.encryption";

        assertEquals( stringMap( key, Encryption.NONE.name() ),
                cv.validate( stringMap( key, Encryption.NONE.name() ) ) );

        expected.expect( InvalidSettingException.class );
        expected.expectMessage(
                "'dbms.connector.http.encryption' is only allowed to be 'NONE'; not 'TLS'" );
        cv.validate( stringMap( key, Encryption.TLS.name() ) );
    }

    @Test
    public void validatesAddress() throws Exception
    {
        String key = "dbms.connector.http.address";

        assertEquals( stringMap( key, "localhost:123" ),
                cv.validate( stringMap( key, "localhost:123" ) ) );

        key = "dbms.connector.bla.address";
        String type = "dbms.connector.bla.type";

        assertEquals( stringMap( key, "localhost:123",
                type, HTTP.name() ),
                cv.validate( stringMap( key, "localhost:123", type, HTTP.name() ) ) );

        assertEquals( stringMap( key, "localhost:123",
                type, HTTP.name() ),
                cv.validate( stringMap( key, "localhost:123", type, HTTP.name() ) ) );

        expected.expect( InvalidSettingException.class );
        expected.expectMessage( "Setting \"dbms.connector.bla.address\" must be in the format \"hostname:port\" or " +
                "\":port\". \"BOBO\" does not conform to these formats" );

        cv.validate( stringMap( key, "BOBO", type, HTTP.name() ) );
    }

    @Test
    public void validatesListenAddress() throws Exception
    {
        String key = "dbms.connector.http.listen_address";

        assertEquals( stringMap( key, "localhost:123" ),
                cv.validate( stringMap( key, "localhost:123" ) ) );

        key = "dbms.connector.bla.listen_address";
        String type = "dbms.connector.bla.type";

        assertEquals( stringMap( key, "localhost:123",
                type, HTTP.name() ),
                cv.validate( stringMap( key, "localhost:123", type, HTTP.name() ) ) );

        assertEquals( stringMap( key, "localhost:123",
                type, HTTP.name() ),
                cv.validate( stringMap( key, "localhost:123", type, HTTP.name() ) ) );

        expected.expect( InvalidSettingException.class );
        expected.expectMessage( "Setting \"dbms.connector.bla.listen_address\" must be in the format " +
                "\"hostname:port\" or \":port\". \"BOBO\" does not conform to these formats" );

        cv.validate( stringMap( key, "BOBO", type, HTTP.name() ) );
    }

    @Test
    public void validatesAdvertisedAddress() throws Exception
    {
        String key = "dbms.connector.http.advertised_address";

        assertEquals( stringMap( key, "localhost:123" ),
                cv.validate( stringMap( key, "localhost:123" ) ) );

        key = "dbms.connector.bla.advertised_address";
        String type = "dbms.connector.bla.type";

        assertEquals( stringMap( key, "localhost:123",
                type, HTTP.name() ),
                cv.validate( stringMap( key, "localhost:123", type, HTTP.name() ) ) );

        assertEquals( stringMap( key, "localhost:123",
                type, HTTP.name() ),
                cv.validate( stringMap( key, "localhost:123", type, HTTP.name() ) ) );

        expected.expect( InvalidSettingException.class );
        expected.expectMessage( "Setting \"dbms.connector.bla.advertised_address\" must be in the format " +
                "\"hostname:port\" or \":port\". \"BOBO\" does not conform to these formats" );

        cv.validate( stringMap( key, "BOBO", type, HTTP.name() ) );
    }

    @Test
    public void validatesType() throws Exception
    {
        String type = "dbms.connector.bla.type";

        expected.expect( InvalidSettingException.class );
        expected.expectMessage( "'dbms.connector.bla.type' must be one of BOLT, HTTP; not 'BOBO'" );

        cv.validate( stringMap( type, "BOBO" ) );
    }

    @Test
    public void unknownSubSettingsAreNotValidated() throws Exception
    {
        String madeup = "dbms.connector.http.imadethisup";

        assertEquals( emptyMap(), cv.validate( stringMap( madeup, "anything" ) ) );
    }
}
