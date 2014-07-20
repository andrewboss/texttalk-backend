require 'spec_helper'

describe 'redis', :type => 'class' do

  context "On a Debian OS with default params" do

    let :facts do
      {
        :osfamily  => 'Debian'
      }
    end # let

    it do
      should contain_class('gcc')
      should contain_class('wget')

      should contain_file('/opt/redis-src').with(:ensure => 'directory',
                                                 :owner => 'root',
                                                 :group => 'root')
      should contain_file('/etc/redis').with(:ensure => 'directory',
                                             :owner => 'root',
                                             :group => 'root')
      should contain_file('redis-lib').with(:ensure => 'directory',
                                            :path   => '/var/lib/redis',
                                            :owner => 'root',
                                            :group => 'root')
      should contain_file("redis-lib-port-6379").with(:ensure => 'directory',
                                                      :path   => '/var/lib/redis/6379',
                                                      :owner => 'root',
                                                      :group => 'root')
      should contain_exec('get-redis-pkg').with_command(/http:\/\/download\.redis\.io\/releases\/redis-2\.8\.11\.tar\.gz/)
      should contain_file('redis-cli-link').with(:ensure => 'link',
                                                 :path   => '/usr/local/bin/redis-cli',
                                                 :target => '/opt/redis/bin/redis-cli')

      should contain_exec('unpack-redis').with(:cwd  => '/opt/redis-src',
                                               :path => '/bin:/usr/bin')
      should contain_exec('install-redis').with(:cwd   => '/opt/redis-src',
                                                :path  => '/bin:/usr/bin')

      should contain_service('redis-6379').with(:ensure => 'running',
                                           :name   => 'redis_6379',
                                           :enable => true)

      should contain_file('redis-init-6379').with(:ensure => 'present',
                                             :path   => '/etc/init.d/redis_6379',
                                             :mode   => '0755',
                                             :owner => 'root',
                                             :group => 'root')
      should contain_file('redis-init-6379').with_content(/^REDIS_BIND_ADDRESS="127.0.0.1"$/)
      should contain_file('redis-init-6379').with_content(/^CLIEXEC="\/opt\/redis\/bin\/redis-cli -h \$REDIS_BIND_ADDRESS -p \$REDIS_PORT/)

      # These values were changed in 2.6.
      should_not contain_file('redis_port_6379.conf').with_content(/maxclients 0/)
      should_not contain_file('redis_port_6379.conf').with_content(/hash-max-zipmap-entries 512/)
      should_not contain_file('redis_port_6379.conf').with_content(/hash-max-zipmap-value 64/)
      should contain_file('redis_port_6379.conf').with_content(/hash-max-ziplist-entries 512/)
      should contain_file('redis_port_6379.conf').with_content(/hash-max-ziplist-value 64/)

      # The bind config should not be present by default.
      should_not contain_file('redis_port_6379.conf').with_content(/bind \d+\.\d+\.\d+\.\d+/)
    end # it
  end # context

  context "On a Debian OS with non-default src and bin locations" do

    let :facts do
      {
        :osfamily  => 'Debian'
      }
    end # let

    let :params do
      {
        :redis_src_dir => '/fake/path/to/redis-src',
        :redis_bin_dir => '/fake/path/to/redis'
      }
    end # let

    it do
      should contain_class('gcc')
      should contain_class('wget')

      should contain_file('/fake/path/to/redis-src').with(:ensure => 'directory')
      should contain_file('/etc/redis').with(:ensure => 'directory')
      should contain_file('redis-lib').with(:ensure => 'directory',
                                            :path   => '/var/lib/redis')
      should contain_file('redis-lib-port-6379').with(:ensure => 'directory',
                                                 :path   => '/var/lib/redis/6379')
      should contain_file('redis-init-6379').with(:ensure => 'present',
                                             :path   => '/etc/init.d/redis_6379',
                                             :mode   => '0755')
      should contain_file('redis-cli-link').with(:ensure => 'link',
                                                 :path   => '/usr/local/bin/redis-cli',
                                                 :target => '/fake/path/to/redis/bin/redis-cli')

      should contain_exec('unpack-redis').with(:cwd  => '/fake/path/to/redis-src',
                                               :path => '/bin:/usr/bin')
      should contain_exec('install-redis').with(:cwd   => '/fake/path/to/redis-src',
                                                :path  => '/bin:/usr/bin')

      should contain_service('redis-6379').with(:ensure => 'running',
                                           :name   => 'redis_6379',
                                           :enable => true)
    end # it
  end # context

  context "On a Debian OS with version 2.6 param" do

    let :facts do
      {
        :osfamily  => 'Debian'
      }
    end # let

    let :params do
      {
        :version => '2.6.4'
      }
    end # let

    it do
      should_not contain_file('redis-pkg')
      should contain_exec('get-redis-pkg').with_command(/http:\/\/download\.redis\.io\/releases\/redis-2\.6\.4\.tar\.gz/)

      # Maxclients is left out for 2.6 unless it is explicitly set.
      should_not contain_file('redis_port_6379.conf').with_content(/maxclients 0/)

      # These params were renamed b/w 2.4 and 2.6.
      should contain_file('redis_port_6379.conf').with_content(/hash-max-ziplist-entries 512/)
      should contain_file('redis_port_6379.conf').with_content(/hash-max-ziplist-value 64/)
      should_not contain_file('redis_port_6379.conf').with_content(/hash-max-zipmap-entries 512/)
      should_not contain_file('redis_port_6379.conf').with_content(/hash-max-zipmap-value 64/)
    end # it
  end # context

  context "With an invalid version param." do
    let :params do
      {
        :version => 'bad version'
      }
    end # let

    it do
      expect { should raise_error(Puppet::Error) }
    end # it
  end # context

  context "On a Debian system with a non-default user specified" do

    let :facts do
      {
        :osfamily  => 'Debian'
      }
    end # let

    let :params do
      { :redis_user => 'my_user' }
    end

    it do
      should contain_file('/opt/redis-src').with(:owner => 'my_user')
      should contain_file('/etc/redis').with(:owner => 'my_user')
      should contain_file('redis-lib').with(:owner => 'my_user')
      should contain_file('redis-lib-port-6379').with(:owner => 'my_user')
      should contain_file('redis-init-6379').with(:owner => 'my_user')
      should_not contain_file('redis-pkg')
    end # it
  end # context

  context "On a Debian system with a non-default group specified" do

    let :facts do
      {
        :osfamily  => 'Debian'
      }
    end # let

    let :params do
      { :redis_group => 'my_group' }
    end

    it do
      should contain_file('/opt/redis-src').with(:group => 'my_group')
      should contain_file('/etc/redis').with(:group => 'my_group')
      should contain_file('redis-lib').with(:group => 'my_group')
      should contain_file('redis-lib-port-6379').with(:group => 'my_group')
      should contain_file('redis-init-6379').with(:group => 'my_group')
      should_not contain_file('redis-pkg')
    end # it
  end # context
end # describe