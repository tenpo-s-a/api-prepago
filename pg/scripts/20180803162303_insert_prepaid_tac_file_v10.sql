--
--    Copyright 2010-2016 the original author or authors.
--
--    Licensed under the Apache License, Version 2.0 (the "License");
--    you may not use this file except in compliance with the License.
--    You may obtain a copy of the License at
--
--       http://www.apache.org/licenses/LICENSE-2.0
--
--    Unless required by applicable law or agreed to in writing, software
--    distributed under the License is distributed on an "AS IS" BASIS,
--    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
--    See the License for the specific language governing permissions and
--    limitations under the License.
--

-- // insert_prepaid_tac_file
-- Migration SQL that makes the change goes here.
INSERT INTO ${schema}.prp_app_file(
    status,
    name,
    version,
    description,
    mime_type,
    location,
    created_at,
    updated_at
) VALUES (
    'ACTIVE',
    'TERMS_AND_CONDITIONS',
    'v1.0',
    'Terminos y Condiciones del producto prepago',
    'text/html',
    '<p>
      <b>Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. </b>
    </p>
    <p>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aenean nec commodo nisl. Donec imperdiet bibendum arcu eget maximus. Sed ac elit suscipit, sodales nunc at, porttitor risus. Pellentesque eget ex placerat justo rutrum feugiat in vel lectus. Aenean felis ligula, dapibus quis fermentum vel, porttitor in massa. Ut nulla justo, ultricies id velit ultrices, vehicula finibus ex. Sed malesuada ante et tempus bibendum. Quisque cursus lacus lectus, non porta est tempor a. Pellentesque ut interdum mauris.</p>
    <p>Praesent elementum, lacus ut sodales pellentesque, leo turpis molestie est, a rutrum enim augue sit amet libero. Suspendisse lacinia fermentum lectus, vel dignissim ipsum ornare id. Morbi viverra lacus ut ex pretium, eu porta ligula dictum. Praesent venenatis eros at facilisis consequat. Etiam auctor elementum ex. Etiam venenatis tristique risus, non pretium quam elementum varius. Etiam vitae molestie sapien. Interdum et malesuada fames ac ante ipsum primis in faucibus. Vivamus sagittis sed ex sit amet consectetur. Duis tortor purus, lobortis eget nibh eu, sollicitudin condimentum urna. Maecenas dictum eu ex eget finibus. Praesent id elit rutrum, scelerisque nulla sed, pharetra massa. Nulla ultrices, dolor at viverra pretium, mi dui cursus diam, elementum venenatis massa augue vitae augue.</p>
    <p>Suspendisse dictum arcu vitae porta venenatis. Vivamus aliquam tincidunt felis. Nulla lorem nunc, pulvinar ac hendrerit vitae, pharetra vel nisl. Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Duis viverra feugiat nulla. In lacinia nunc vitae lacus ultricies ornare. Donec sagittis, neque ut commodo commodo, libero lorem efficitur enim, tristique tincidunt ipsum quam sed purus. Sed eget posuere sapien. Morbi arcu massa, bibendum et egestas eu, viverra at neque.</p>
    <p>Aliquam erat volutpat. Cras felis tellus, scelerisque suscipit congue non, congue mollis metus. Sed elementum ipsum ultrices vulputate fermentum. Curabitur tortor est, tempus ac euismod vel, fermentum vel magna. Fusce laoreet malesuada sem, vitae condimentum sem dictum sit amet. Cras luctus ullamcorper magna, eget semper justo. Aenean at arcu ligula. In eu nisi eu libero pellentesque sodales ac at purus. Curabitur scelerisque, sapien in ultrices viverra, ex dolor elementum nulla, et euismod metus odio vel elit. Vestibulum sit amet justo vitae purus dictum tempor. Fusce iaculis cursus eros. Suspendisse sodales elit tellus, non pellentesque dolor interdum sit amet. Donec et libero eu augue blandit rhoncus at nec nibh.</p>
    <p>Curabitur fermentum nibh sed nibh consectetur, id pharetra augue luctus. Maecenas et varius sapien, non ultricies dolor. Proin volutpat, libero id rutrum convallis, dui metus vehicula elit, sed facilisis tellus lorem lacinia ipsum. Phasellus gravida metus eu nibh commodo, ut luctus quam rutrum. Vestibulum vel nisi in orci tempor viverra vel nec erat. Aliquam laoreet mi in dui pellentesque, eget aliquet leo tempor. Vestibulum nunc metus, hendrerit in erat eu, faucibus faucibus erat. Pellentesque et tellus efficitur ligula rhoncus consectetur. Mauris tincidunt magna ut suscipit volutpat.</p>',
    timezone('utc', now()),
    timezone('utc', now())
)


-- //@UNDO
-- SQL to undo the change goes here.
DELETE FROM ${schema}.prp_app_file WHERE name = 'TERMS_AND_CONDITIONS' AND version = 'v1.0';

